/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import at.tfr.pfad.SquadType;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Function;
import at.tfr.pfad.model.Member;

@Stateless
public class MemberValidator {

	public static String GEB_UNVOLL = "Geburtsdatum unvollständig";
	public static String ZU_JUNG = "Zu jung für ";
	public static String ZU_ALT = "Zu alt für ";
	public static String INAKTIV_IM_TRUPP = "Inaktives Mitlgied im Trupp";
	public static String KEIN_TRUPP_FUNKTION = "Weder Trupp noch Funktion";
	public static String INAKTIV_ALS_LEITER = "Inaktiv als Leiter/Assistent";

	@Inject
	private SquadRepository squadRepo;

	public boolean isGrinsExportable(Member m, Collection<Member> leaders) {
		return m.isAktiv() || leaders.contains(m)
				|| m.getFunktionen().stream().anyMatch(f -> Boolean.TRUE.equals(f.getExportReg()));
	}

	public List<ValidationResult> validate(Member member, final Collection<Member> leaders) {
		return validate(member, "", leaders);
	}

	public List<ValidationResult> validate(Member member, final String funktionen, final Collection<Member> leaders) {
		List<ValidationResult> results = new ArrayList<>();

		if (StringUtils.isBlank(member.getName())) {
			results.add(new ValidationResult(false, "Name darf nicht leer sein."));
		}
		
		if (StringUtils.isBlank(member.getVorname())) {
			results.add(new ValidationResult(false, "Vorname darf nicht leer sein."));
		}
		
		List<Function> funcExp = member.getFunktionen().stream().filter(f -> f.getExportReg())
				.collect(Collectors.toList());
		if (!member.isAktiv() && !funcExp.isEmpty()) {
			results.add(new ValidationResult(false, "Inaktiv mit " + funcExp));
		}

		if (!member.isAktiv() && leaders.contains(member)) {
			results.add(new ValidationResult(false, INAKTIV_ALS_LEITER + ": " + squadRepo.findByResponsible(member)));
		}

		if (member.isAktiv() && member.getVollzahler() != null && member.getVollzahler().getVollzahler() != null) {
			results.add(new ValidationResult(false, "Vollzahler ist KEIN Vollzahler: " + member.getVollzahler()
					+ " (dessen Vollzahler: " + member.getVollzahler().getVollzahler() + ")"));
		}

		if (member.isAktiv() && member.getVollzahler() != null
				&& !(member.getVollzahler().isAktiv() || member.getVollzahler().isAktivExtern())) {
			results.add(new ValidationResult(false, "Vollzahler INAKTIV: " + member.getVollzahler()));
		}

		if (member.isAktiv() && member.getVollzahler() != null
				&& member.getVollzahler().geburtstag().isAfter(member.geburtstag())) {
			results.add(new ValidationResult(false,
					"Vollzahler SOLL älter sein, als das ermäßigte Mitglied: " + member.getVollzahler()));
		}

		if (member.getTrupp() != null || isGrinsExportable(member, leaders)) {
			if (member.getGebJahr() < 1900 || member.getGebMonat() < 1 || member.getGebTag() < 1) {
				results.add(new ValidationResult(false, GEB_UNVOLL));
			}
		}

		if (member.getTrupp() != null) {

			if (!member.isAktiv()) {
				results.add(new ValidationResult(false, INAKTIV_IM_TRUPP));
			}

			SquadType type = member.getTrupp().getType();
			if (type == null) {
				results.add(new ValidationResult(false, "INVALID SquadType==Null"));
			} else {
				if ((new DateTime().getYear() - member.getGebJahr()) < type.getMin() - 1) {
					results.add(new ValidationResult(false, ZU_JUNG + type));
				}
				if ((new DateTime().getYear() - member.getGebJahr()) > type.getMax() + 1) {
					results.add(new ValidationResult(false, ZU_ALT + type));
				}
			}
		} else {
			if (member.isAktiv() && StringUtils.isBlank(funktionen) && funcExp.isEmpty() && !leaders.contains(member)) {
				results.add(new ValidationResult(false, KEIN_TRUPP_FUNKTION));
			}
		}

		return results;
	}

}
