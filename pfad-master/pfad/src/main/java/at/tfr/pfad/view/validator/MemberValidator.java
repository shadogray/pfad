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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import at.tfr.pfad.SquadType;
import at.tfr.pfad.dao.FunctionRepository;
import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Function;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Squad;

@Stateless
public class MemberValidator {

	public static String GEB_UNVOLL = "Geburtsdatum unvollständig";
	public static String ZU_JUNG = "Zu jung für ";
	public static String ZU_ALT = "Zu alt für ";
	public static String INAKTIV_IM_TRUPP = "Inaktives Mitlgied im Trupp";
	public static String KEIN_TRUPP_FUNKTION = "Weder Trupp noch Funktion";
	public static String INAKTIV_ALS_LEITER = "Inaktiv als Leiter/Assistent";

	@Inject
	private MemberRepository memberRepo;
	@Inject
	private SquadRepository squadRepo;
	@Inject 
	private FunctionRepository functionRepo;

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
		
		// Assistenten in einer Stufe oder ‚ZBV‘, beides ist nicht sinnvoll
		List<Squad> trupps = squadRepo.findByAssistant(member);
		if (!trupps.isEmpty() && funcExp.stream().anyMatch(f -> "ZBV".equals(f.getKey())) ) {
			results.add(new ValidationResult(false, "ZBV und Assistent in "+ trupps + " - Nicht sinnvoll!: " + squadRepo.findByResponsible(member)));
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

//		if (member.isAktiv() && member.getVollzahler() != null
//				&& member.getVollzahler().geburtstag().isAfter(member.geburtstag())) {
//			results.add(new ValidationResult(false,
//					"Vollzahler SOLL älter sein, als das ermäßigte Mitglied: " + member.getVollzahler()));
//		}

		if (member.getTrupp() != null || isGrinsExportable(member, leaders)) {
			if (member.getGebJahr() < 1900 || member.getGebMonat() < 1 || member.getGebTag() < 1) {
				results.add(new ValidationResult(false, GEB_UNVOLL));
			}
		}

		if (member.getTrupp() != null) {

			if (!member.isAktiv()) {
				results.add(new ValidationResult(false, INAKTIV_IM_TRUPP + ": " + member.getTrupp()));
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

		// Es kann nur eine GFW oder GFM geben! Für Kathi Fosen würde ein GFA passend sein
		if (!member.getFunktionen().isEmpty()) {
			
			Predicate<Function> gfmOrGfw = f -> "GFW".equals(f.getKey()) || "GFM".equals(f.getKey());
			
			if (member.getFunktionen().stream().anyMatch(gfmOrGfw)) {
				Collection<Function> functs = functionRepo.findAll().stream().filter(gfmOrGfw).collect(Collectors.toList());
				for (Function funct : functs) {
					if (member.getFunktionen().contains(funct)) {
						List<Member> gfmOrGfws = memberRepo.findByFunction(funct).stream().
								filter(m -> member != m && m.getFunktionen().contains(funct)).collect(Collectors.toList());
						if (!gfmOrGfws.isEmpty()) {
							results.add(new ValidationResult(false, "Nur ein GFM o. GFW zulässig: "+gfmOrGfws+" : "+funct));
						}
					}
				}
			}
		}
		
		return results;
	}
	

}
