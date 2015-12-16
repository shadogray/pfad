/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.transform.stream.StreamSource;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.joda.time.DateTime;

import com.google.common.net.HttpHeaders;

import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Squad;

@Named
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class DownloadBean {

	enum HeaderRegistrierung {
		BVKey, GruppenSchlussel, PersonenKey, Titel, Name, Vorname, Anrede, GebTag, GebMonat, GebJahr, Straße, PLZ, Ort, Geschlecht, Aktiv, Vollzahler, Email, Telefon, Funktionen, Trupp, OK
	}

	enum HeaderLocal {
		Religion, FunktionenBaden, Trail, Gilde, AltER
	}

	enum DataStructure {
		XLS, CSV
	}

	@Inject
	private MemberRepository membRepo;
	@Inject
	private SquadRepository squadRepo;
	@Inject
	private MemberBean memberBean;
	@Inject
	private SquadBean squadBean;
	@Resource
	private SessionContext sessionContext;

	public String downloadRegistrierung() throws Exception {
		return downloadData(false);
	}

	public String downloadAll() throws Exception {
		return downloadData(true);
	}
	
	public String downloadSquad(Squad squad) throws Exception {
		return downloadData(true, squad);
	}

	public boolean isDownloadAllowed() {
		return isDownloadAllowed(new Squad[]{});
	}

	public boolean isDownloadAllowed(Squad...squads) {
		if (memberBean.isAdmin() || memberBean.isGruppe() || 
				(memberBean.isLeiter() && squads != null && Stream.of(squads).allMatch(s -> squadBean.isUpdateAllowed(s))))
			return true;
		return false;
	}
	
	public String downloadData(boolean withLocal, Squad...squads) throws Exception {

		if (!isDownloadAllowed(squads))
			throw new SecurityException(
					"user may not download: " + memberBean.getSessionContext().getCallerPrincipal());

		ExternalContext ectx = setHeaders();
		try (OutputStream os = ectx.getResponseOutputStream()) {
			HSSFWorkbook wb = generateData(withLocal, squads);
			wb.write(os);
		}
		FacesContext.getCurrentInstance().responseComplete();

		return "";
	}

	private HSSFWorkbook generateData(boolean withLocal, Squad...squads) {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Export_" + DateTime.now().toString("yyyy.mm.dd"));

		int rCount = 0;
		HSSFRow row = sheet.createRow(rCount++);

		List<String> headers = Arrays.asList(HeaderRegistrierung.values()).stream().map(h -> h.name())
				.collect(Collectors.toList());
		if (withLocal) {
			headers.addAll(
					Arrays.asList(HeaderLocal.values()).stream().map(h -> h.name()).collect(Collectors.toList()));
		}

		for (int i = 0; i < headers.size(); i++) {
			String h = headers.get(i);
			HSSFCell c = row.createCell(i);
			c.setCellValue(h);
		}

		Collection<Member> leaders = squadRepo.findLeaders();

		for (Member m : getMembers()) {

			if (!withLocal && !(m.isAktiv() || leaders.contains(m)
					|| m.getFunktionen().stream().filter(f -> f.isExportReg()).count() > 0))
				continue;
			
			if (squads != null) {
				if (!Stream.of(squads).anyMatch(s->s.equals(m.getTrupp()))) {
					continue;
				}
			}

			ValidationResult vr = validate(m);

			row = sheet.createRow(rCount++);
			if (!vr.valid) {
				row.getRowStyle().setFillBackgroundColor(HSSFColor.RED.index);
			}

			int cCount = 0;
			row.createCell(cCount++).setCellValue(m.getBVKey());
			row.createCell(cCount++).setCellValue(m.getGruppenSchluessel());
			row.createCell(cCount++).setCellValue(m.getPersonenKey());
			row.createCell(cCount++).setCellValue(m.getTitel());
			row.createCell(cCount++).setCellValue(m.getName());
			row.createCell(cCount++).setCellValue(m.getVorname());
			row.createCell(cCount++).setCellValue(m.getAnrede());
			row.createCell(cCount++).setCellValue(m.getGebTag());
			row.createCell(cCount++).setCellValue(m.getGebMonat());
			row.createCell(cCount++).setCellValue(m.getGebJahr());
			row.createCell(cCount++).setCellValue(m.getStrasse());
			row.createCell(cCount++).setCellValue(m.getPLZ());
			row.createCell(cCount++).setCellValue(m.getOrt());
			row.createCell(cCount++).setCellValue(m.getGeschlecht().name());
			row.createCell(cCount++).setCellValue(m.isAktiv() ? "J" : "N");
			row.createCell(cCount++).setCellValue(m.getVollzahler() != null ? m.getVollzahler().getBVKey() : "");
			row.createCell(cCount++).setCellValue(m.getEmail());
			row.createCell(cCount++).setCellValue(m.getTelefon());
			row.createCell(cCount++).setCellValue(getFunktionen(m));
			row.createCell(cCount++).setCellValue(m.getTrupp() != null ? m.getTrupp().getName() : "");
			row.createCell(cCount++).setCellValue(vr.message);

			// and Local Data
			if (withLocal) { // Religion, FunktionenBaden, Trail, Gilde, AltER
				row.createCell(cCount++).setCellValue(m.getReligion());
				row.createCell(cCount++).setCellValue("");
				row.createCell(cCount++).setCellValue(m.isTrail() ? "X" : "");
				row.createCell(cCount++).setCellValue(m.isGilde() ? "X" : "");
				row.createCell(cCount++).setCellValue(m.isAltER() ? "X" : "");
			}

		}
		return wb;
	}

	private List<Member> getMembers() {
		List<Member> members = membRepo.findAll().stream().sorted().collect(Collectors.toList());
		if (sessionContext.isCallerInRole(Roles.admin.name()) || sessionContext.isCallerInRole(Roles.gruppe.name()))
			return members;
		if (sessionContext.isCallerInRole(Roles.leiter.name())) {
			List<Squad> squads = squadRepo.findByName(sessionContext.getCallerPrincipal().getName());
			members = members.stream()
					.filter(m -> m.getTrupp() != null && squads.contains(m.getTrupp()))
					.collect(Collectors.toList());
		}
		return members;
	}

	private ExternalContext setHeaders() {
		ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
		ectx.responseReset();
		DataStructure dataStructure = DataStructure.XLS;
		String encoding = "UTF8";

		if (DataStructure.XLS.equals(dataStructure)) {
			ectx.setResponseContentType("application/excel");
			ectx.setResponseCharacterEncoding("binary");
		} else {
			ectx.setResponseContentType("application/csv");
			ectx.setResponseCharacterEncoding(encoding);
		}
		ectx.setResponseHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Export_"
				+ DateTime.now().toString("yyyymmdd_HHMM") + "." + dataStructure.name().toLowerCase());
		return ectx;
	}

	String getFunktionen(Member m) {
		StringBuilder sb = new StringBuilder();
		if (m.getTrupp() != null) {
			sb.append(m.getTrupp().getType().getKey(m.getGeschlecht())).append(" ");
		}
		List<String> toLead = squadRepo.findByLeaderFemaleEqualOrLeaderMaleEqual(m).stream()
				.map(s -> "AS" + s.getType().getKey(m.getGeschlecht())).collect(Collectors.toList());
		List<String> toAss = squadRepo.findByAssistant(m).stream()
				.map(s -> "AS" + s.getType().getKey(m.getGeschlecht())).collect(Collectors.toList());
		toLead.addAll(toAss);
		sb.append(toLead.stream().distinct().collect(Collectors.joining(" ")));
		sb.append(" ");
		if (!m.getFunktionen().isEmpty()) {
			sb.append(m.getFunktionen().stream().map(f -> f.getKey()).collect(Collectors.joining(" ")));
		}
		return sb.toString().trim();
	}

	public ValidationResult validate(Member member) {
		if (member.isAktiv() && member.getVollzahler() != null && !member.getVollzahler().isAktiv()) {
			return new ValidationResult(false, "Vollzahler INAKTIV");
		}
		return new ValidationResult(true, "");
	}

	public static class ValidationResult {
		public boolean valid;
		public String message;

		public ValidationResult() {
		}

		public ValidationResult(boolean valid, String message) {
			super();
			this.valid = valid;
			this.message = message;
		}
	}

}
