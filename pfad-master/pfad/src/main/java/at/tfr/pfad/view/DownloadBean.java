/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.ejb.Stateful;
import javax.el.ValueExpression;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.html.HtmlColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.jboss.logging.Logger;
import org.joda.time.DateTime;

import com.google.common.net.HttpHeaders;

import at.tfr.pfad.ConfigurationType;
import at.tfr.pfad.Role;
import at.tfr.pfad.dao.ActivityRepository;
import at.tfr.pfad.dao.BookingRepository;
import at.tfr.pfad.dao.ConfigurationRepository;
import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Configuration;
import at.tfr.pfad.model.Function;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Squad;
import at.tfr.pfad.view.validator.MemberValidator;
import at.tfr.pfad.view.validator.ValidationResult;

@Named
@SessionScoped
@Stateful
public class DownloadBean implements Serializable {

	private Logger log = Logger.getLogger(getClass());

	enum HeaderRegistrierung {
		BVKey, GruppenSchlussel, PersonenKey, Titel, Name, Vorname, Anrede, GebTag, GebMonat, GebJahr, Straße, PLZ, Ort, Geschlecht, Aktiv, Vollzahler, Email, Religion, Telefon, Funktionen,
	}

	enum HeaderGruppe {
		Key, Name, Heim1, Straße1, Plz1, Ort1, Heim2, Straße2, Plz2, Ort2, BIC, IBAN, Bezirk, Web, Mail, Gruendungsjahr, Verein, Letzte_Wahl_GFm, Letzte_Wahl_GFw, Letzte_Wahl_ER, ZVR, BLZ, KontoNr;
	}

	enum HeaderLocal {
		OK, Trupp, Religion, FunktionenBaden, Trail, Gilde, AltER, InfoMail, Mitarbeit, Eltern, Kinder, KinderTrupps
	}

	enum DataStructure {
		XLS, CSV, XLSX
	}
	
	@Inject
	private MemberRepository membRepo;
	@Inject
	private SquadRepository squadRepo;
	@Inject
	private ConfigurationRepository configRepo;
	@Inject
	private ActivityRepository activityRepo;
	@Inject
	private BookingRepository bookingRepo;
	@Inject
	private SquadBean squadBean;
	@Inject
	private SessionBean sessionBean;
	@Inject
	private MemberValidator memberValidator;
	@Inject
	private EntityManager em;
	private Configuration configuration;
	private String query;
	private boolean nativeQuery;
	private List<List<?>> results = new ArrayList<>();
	private HtmlPanelGroup dataTableGroup;
	public static final String SafeDatePattern = "yyyy.MM.dd_HHmm";

	public String downloadVorRegistrierung() throws Exception {
		Collection<Member> leaders = squadRepo.findLeaders();
		Predicate<Member> filter = 
				m -> (leaders.contains(m) || m.getFunktionen().stream().anyMatch(f -> Boolean.TRUE.equals(f.getExportReg())));
		return downloadData(false, filter);
	}

	public String downloadRegistrierung() throws Exception {
		return downloadData(false, null);
	}

	public String downloadAll() throws Exception {
		return downloadData(true, null, sessionBean.getSquad());
	}

	public String downloadAllWithBookings() throws Exception {
		return downloadData(true, true, null, sessionBean.getSquad());
	}

	public String downloadSquad(Squad squad) throws Exception {
		return downloadData(true, null, squad);
	}

	public boolean isDownloadAllowed() {
		return isDownloadAllowed(new Squad[] {});
	}

	public boolean isDownloadAllowed(Squad... squads) {
		if (sessionBean.isAdmin() || sessionBean.isGruppe() || sessionBean.isVorstand() || (sessionBean.isLeiter() && squads != null
				&& Stream.of(squads).allMatch(s -> squadBean.isDownloadAllowed(s))))
			return true;
		return false;
	}

	public String downloadData(boolean withLocal, Predicate<Member> filter, Squad... squads) throws Exception {
		return downloadData(withLocal, false, filter, squads);
	}
	
	public String downloadData(boolean withLocal, boolean withBookings, Predicate<Member> filter, Squad... squads) throws Exception {
		try {

			if (!isDownloadAllowed(squads))
				throw new SecurityException(
						"user may not download: " + sessionBean.getUserSession().getCallerPrincipal());

			ExternalContext ectx = setHeaders("Export");
			try (OutputStream os = ectx.getResponseOutputStream()) {
				HSSFWorkbook wb = generateData(withLocal, withBookings, filter, squads);
				wb.write(os);
			}
			FacesContext.getCurrentInstance().responseComplete();

		} catch (Exception e) {
			log.info("executeQuery: " + e, e);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), null));
		}

		return "";
	}

	private HSSFWorkbook generateData(boolean withLocal, boolean withBookings, Predicate<Member> filter, Squad... squads) {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Personen");
		CellStyle red = wb.createCellStyle();
		red.setFillForegroundColor(HSSFColor.RED.index);
		red.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		squads = Stream.of(squads).filter(s->s != null).collect(Collectors.toList()).toArray(new Squad[]{});

		// non-filtered members
		if (filter == null) {
			filter = m -> true;
		}
		
		int rCount = 0;
		HSSFRow row = sheet.createRow(rCount++);
		
		List<Activity> activities = activityRepo.findAll().stream().filter(a->!a.isFinished())
				.sorted((a,b) -> a.getName().compareTo(b.getName())).collect(Collectors.toList());

		List<String> headers = Arrays.asList(HeaderRegistrierung.values()).stream().map(h -> h.name())
				.collect(Collectors.toList());
		if (withLocal) {
			headers.addAll(
					Arrays.asList(HeaderLocal.values()).stream().map(h -> h.name()).collect(Collectors.toList()));
		}
		if (withBookings) {
			headers.add("--");
			headers.addAll(activities.stream().map(a->a.getName()).collect(Collectors.toList()));
		}

		for (int i = 0; i < headers.size(); i++) {
			String h = transformHeaders(headers, i);
			HSSFCell c = row.createCell(i);
			c.setCellValue(h);
		}

		Collection<Member> leaders = squadRepo.findLeaders();
		List<Member> members = getMembers().stream().filter(filter).collect(Collectors.toList());

		for (Member m : members) {
			
			if (!withLocal && memberValidator.isNotGrinsExportable(m, leaders))
				continue;

			if (squads != null && squads.length > 0) {
				if (!Stream.of(squads).anyMatch(s -> s.equals(m.getTrupp()))) {
					continue;
				}
			}

			List<ValidationResult> vr = memberValidator.validate(m, getFunktionen(m), leaders);

			row = sheet.createRow(rCount++);

			int cCount = 0;
			// BVKeyBVKey
			row.createCell(cCount++).setCellValue(m.getBVKey());
			// GruppenSchlussel
			row.createCell(cCount++).setCellValue(m.getGruppenSchluessel());
			// PersonenKey
			row.createCell(cCount++).setCellValue(m.getPersonenKey());
			// Titel
			row.createCell(cCount++).setCellValue(m.getTitel());
			// Name
			row.createCell(cCount++).setCellValue(m.getName());
			// Vorname
			row.createCell(cCount++).setCellValue(m.getVorname());
			// Anrede
			row.createCell(cCount++).setCellValue(m.getAnrede());
			// GebTag
			row.createCell(cCount++).setCellValue(m.getGebTag());
			// GebMonat
			row.createCell(cCount++).setCellValue(m.getGebMonat());
			// GebJahr
			row.createCell(cCount++).setCellValue(m.getGebJahr());
			// Straße
			row.createCell(cCount++).setCellValue(m.getStrasse());
			// PLZ
			row.createCell(cCount++).setCellValue(m.getPLZ());
			// Ort
			row.createCell(cCount++).setCellValue(m.getOrt());
			// Geschlecht
			row.createCell(cCount++).setCellValue(m.getGeschlecht() != null ? m.getGeschlecht().name() : "");
			// Aktiv
			row.createCell(cCount++).setCellValue(m.isAktiv() ? "J" : "N");
			// Vollzahler
			row.createCell(cCount++).setCellValue(m.getFunktionen().stream().anyMatch(f->Function.PTA.equals(f.getKey())) ? "P" : (m.getVollzahler() != null ? m.getVollzahler().getBVKey() : "N"));
			// Email
			row.createCell(cCount++).setCellValue(m.getEmail());
			// Religion
			row.createCell(cCount++).setCellValue(withLocal ? m.getReligion() : ""); // do not return Religion
			// Telefon
			row.createCell(cCount++).setCellValue(m.getTelefon());
			// Funktionen
			row.createCell(cCount++).setCellValue(getFunktionen(m));

			// and Local Data
			if (withLocal) { // Religion, FunktionenBaden, Trail, Gilde, AltER

				HSSFCell ok = row.createCell(cCount++);
				if (!vr.isEmpty()) {
					ok.setCellValue(vr.stream().map(v -> v.getMessage()).collect(Collectors.joining(",")));
					ok.setCellStyle(red);
				}

				row.createCell(cCount++).setCellValue(m.getTrupp() != null ? m.getTrupp().getName() : "");
				row.createCell(cCount++).setCellValue(m.getReligion());
				row.createCell(cCount++).setCellValue("");
				row.createCell(cCount++).setCellValue(m.isTrail() ? "X" : "");
				row.createCell(cCount++).setCellValue(m.isGilde() ? "X" : "");
				row.createCell(cCount++).setCellValue(m.isAltER() ? "X" : "");
				row.createCell(cCount++).setCellValue(m.isInfoMail() ? "X" : "");
				row.createCell(cCount++).setCellValue(m.isSupport() ? "X" : "");
				// Eltern, Kinder, KinderTrupps
				row.createCell(cCount++).setCellValue(!m.getSiblings().isEmpty() ? "X" : "");
				row.createCell(cCount++)
						.setCellValue(m.getSiblings().stream().map(s -> s.toString()).collect(Collectors.joining(",")));
				row.createCell(cCount++).setCellValue(m.getSiblings().stream()
						.map(s -> s.getTrupp() != null ? s.getTrupp().getName() : "").collect(Collectors.joining(",")));
			}
			
			if (withBookings) {
				row.createCell(cCount++).setCellValue("");
				for (Activity a : activities) {
					Optional<Booking> bOpt = m.getBookings().stream()
							.filter(b-> b.isValid() && a.equals(b.getActivity())).findAny();
					row.createCell(cCount++).setCellValue(bOpt.isPresent() ? ""+a.getName() : "");
				}
			}

		}

		formatGruppeSheet(wb.createSheet("Gruppe"));
		formatStatusSheet(wb.createSheet("Status"));

		return wb;
	}

	private HSSFSheet formatGruppeSheet(HSSFSheet sheet) {
		// Key Name Heim1 Straße1 Plz1 Ort1 Heim2 Straße2 Plz2 Ort2 BIC IBAN
		// Bezirk Web Mail
		// Gründungsjahr Verein Letzte Wahl GFm Letzte Wahl GFw Letzte Wahl ER
		// ZVR BLZ KontoNr
		// BAD Baden Marchetstraße 7 2500 Baden 0 0 www.ontrail.at
		// vorstand@ontrail.at
		// 22.06.14 22.06.14 2015-11-18 0 0
		List<String> headers = transformGruppeHeaders();
		HSSFRow row = sheet.createRow(0);
		for (int i = 0; i < headers.size(); i++) {
			row.createCell(i).setCellValue(headers.get(i));
		}
		row = sheet.createRow(1);
		int cCount = 0;
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Key.name(), "BAD"));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Name.name(), "Baden"));
		row.createCell(cCount++)
				.setCellValue(configRepo.getValue(HeaderGruppe.Heim1.name(), "Fritz Fangl Pfadfinderheim"));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Straße1.name(), "Marchetstraße 7"));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Plz1.name(), "2500"));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Ort1.name(), "Baden"));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Heim2.name(), ""));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Straße2.name(), ""));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Plz2.name(), ""));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Ort2.name(), ""));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.BIC.name(), ""));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.IBAN.name(), ""));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Bezirk.name(), "Baden"));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Web.name(), "www.ontrail.at"));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Mail.name(), "vorstand@ontrail.at"));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Gruendungsjahr.name(), "1930"));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Verein.name(), ""));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Letzte_Wahl_GFw.name(), "22.06.2014"));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Letzte_Wahl_GFm.name(), "22.06.2014"));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.Letzte_Wahl_ER.name(), "18.11.2015"));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.ZVR.name(), "545163933"));
		row.createCell(cCount++).setCellValue(configRepo.getValue(HeaderGruppe.BLZ.name(), "20205"));
		return sheet;
	}

	private HSSFSheet formatStatusSheet(HSSFSheet sheet) {
		int rCount = 0;
		// "Export_" + DateTime.now().toString("yyyy.mm.dd"));
		// Die Auswertung wurde mit folgenden Optionen erstellt:
		HSSFRow row = sheet.createRow(rCount++);
		row.createCell(0).setCellValue("Die Auswertung wurde mit folgenden Optionen erstellt:");
		// Gruppenkürzel BAD
		row = sheet.createRow(rCount++);
		row.createCell(0).setCellValue("Gruppenkürzel");
		row.createCell(1).setCellValue(configRepo.getValue(HeaderGruppe.Key.name(), "BAD"));
		// Auswertung vom 23.12.2015 10:22
		row = sheet.createRow(rCount++);
		row.createCell(0).setCellValue("Auswertung vom");
		row.createCell(1).setCellValue(DateTime.now().toString("dd.MM.yyyy HH:mm"));
		// GRINS Art HR
		row = sheet.createRow(rCount++);
		row.createCell(0).setCellValue("GRINS Art");
		row.createCell(1).setCellValue(configRepo.getValue("GRINS_Art", "HR"));
		// Benutzer reg
		row = sheet.createRow(rCount++);
		row.createCell(0).setCellValue("Benutzer");
		row.createCell(1).setCellValue(sessionBean.getUser().getName());
		// Akzeptierte Fehler
		row = sheet.createRow(rCount++);
		row.createCell(0).setCellValue("Akzeptierte Fehler");
		row.createCell(1).setCellValue("");

		return sheet;
	}

	private List<String> transformGruppeHeaders() {
		return Stream.of(HeaderGruppe.values())
				.map(h -> h.name().replaceAll("_", " ").replaceAll("ue", "ü").replaceAll("oe", "ö"))
				.collect(Collectors.toList());
	}

	private String transformHeaders(List<String> headers, int i) {
		String h = headers.get(i);
		switch (h) {
		case "Vollzahler":
			h = "Ermäßigt";
			break;
		case "BVKey":
			h = "BV-Key";
			break;
		case "GruppenSchlussel":
			h = "GruppenSchlüssel";
			break;
		case "PLZ":
			h = "Postleitzahl";
			break;
		}
		return h;
	}

	private List<Member> getMembers() {
		List<Member> members = membRepo.findAll().stream().sorted().collect(Collectors.toList());
		if (sessionBean.isAdmin() || sessionBean.isGruppe() || sessionBean.isVorstand())
			return members;
		if (sessionBean.isLeiter()) {
			Squad squad = sessionBean.getSquad();
			members = members.stream().filter(m -> m.getTrupp() != null && m.getTrupp().equals(squad))
					.collect(Collectors.toList());
		}
		return members;
	}

	public static ExternalContext setHeaders(String prefix) {
		DataStructure dataStructure = DataStructure.XLS;
		String encoding = "UTF8";
		return setHeaders(prefix, dataStructure, encoding);
	}

	public static ExternalContext setHeaders(String prefix, DataStructure dataStructure) {
		return setHeaders(prefix, dataStructure, "UTF-8");
	}
	
	public static ExternalContext setHeaders(String prefix, DataStructure dataStructure, String encoding) {
		ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
		ectx.responseReset();

		String suffix = dataStructure != null ? dataStructure.name().toLowerCase() : "bin";

		switch (dataStructure) {
		case XLS:
			ectx.setResponseContentType("application/excel");
			ectx.setResponseCharacterEncoding("binary");
			break;
		case XLSX:
			ectx.setResponseContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			ectx.setResponseCharacterEncoding("binary");
			break;
		default:
			ectx.setResponseContentType("application/csv");
			ectx.setResponseCharacterEncoding(encoding);
		}
		
		ectx.setResponseHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+prefix+"_"
				+ DateTime.now().toString(SafeDatePattern) + "." + suffix);
		return ectx;
	}

	private String getFunktionen(Member m) {
		List<String> functions = new ArrayList<>();
		if (m.getTrupp() != null) {
			functions.add(m.getTrupp().getType().getKey(m.getGeschlecht()));
		}

		Collection<Function> funktionen = m.getFunktionen().stream()
				.filter(f -> f.getKey() != null).collect(Collectors.toList());
		
		List<String> toLead = squadRepo.findByLeaderFemaleEqualOrLeaderMaleEqual(m).stream()
				.map(s -> "AS" + s.getType().getKey(m.getGeschlecht())).collect(Collectors.toList());
		
		List<String> toAss = squadRepo.findByAssistant(m).stream()
				.map(s -> "AS" + s.getType().getKey(m.getGeschlecht())).collect(Collectors.toList());
		
		if (!funktionen.isEmpty()) {
			functions.addAll(funktionen.stream()
					.filter(f -> !Function.PTA.equals(f.getKey()))
					.filter(f -> Boolean.FALSE.equals(f.getNoFunction()))
					.map(f -> f.getKey()).collect(Collectors.toList()));
		}

		// Sobald aber jemand die Bezeichnung SF oder AS vor der Stufenbezeichnung hat, dann ist er definitiv kein ZBV mehr
		if (!toLead.isEmpty() || !toAss.isEmpty()) {
			funktionen = funktionen.stream()
					.filter(f -> !f.getKey().equals(Function.ZBV)).collect(Collectors.toList());
		}
		
		if (!funktionen.stream().anyMatch(f -> Boolean.TRUE.equals(f.getLeader()))) {
			if (!functions.stream().anyMatch(f->(f.startsWith("SF") || f.startsWith("GF")))) {
				functions.addAll(toLead);
				functions.addAll(toAss);
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append(functions.stream().distinct().collect(Collectors.joining(",")));
		return sb.toString().trim();
	}
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public boolean isNativeQuery() {
		return nativeQuery;
	}

	public void setNativeQuery(boolean nativeQuery) {
		this.nativeQuery = nativeQuery;
	}

	public List<List<?>> getResults() {
		return results;
	}

	public List<Integer> getResultIndexes() {
		return IntStream.range(0, results.size()).boxed().collect(Collectors.toList());
	}

	public List<Configuration> getQueries() {
		return configRepo.findByTypeOrderByCkeyAsc(ConfigurationType.query).stream()
				.filter(q -> sessionBean.isAdmin() || Role.none.equals(q.getRole())
						|| sessionBean.getUserSession().isCallerInRole(q.getRole().name()))
				.collect(Collectors.toList());
	}

	public void executeQuery(Long configurationId) {
		try {
			Optional<Configuration> confOpt = getQueries().stream().filter(q -> configurationId.equals(q.getId()))
					.findFirst();
			if (confOpt.isPresent()) {
				configuration = confOpt.get();
				query = confOpt.get().getCvalue();
				nativeQuery = ConfigurationType.nativeQuery.equals(confOpt.get().getType());
				query();
			}
		} catch (Exception e) {
			log.info("executeQuery: " + e, e);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), null));
		}
	}

	@SuppressWarnings("unchecked")
	public String query() {
		Query q = null;
		if (dataTableGroup != null) {
			dataTableGroup.getChildren().clear();
		}
		try {
			results = new ArrayList<>();
			List<?> res;
			if (nativeQuery) {
				res = em.createNativeQuery(query).getResultList();
			} else {
				q = em.createQuery(query);
				res = q.getResultList();
			}
			if (res.size() > 0) {
				if (res.get(0) instanceof Tuple) {
					results = ((List<Tuple>) res).stream().map(r -> r.getElements()).collect(Collectors.toList());
				} else if (res.get(0) instanceof Object[]) {
					results = res.stream().map(r -> Arrays.asList((Object[]) r)).collect(Collectors.toList());
				} else {
					results = res.stream().map(r -> Arrays.asList(new Object[] { r })).collect(Collectors.toList());
				}
			}
			if (dataTableGroup != null && results != null && results.size() > 0) {
				dataTableGroup.getChildren().add(populateDataTable(results));
				dataTableGroup = null;
			}
		} catch (Exception e) {
			log.info("cannot execute: " + q + " : " + e, e);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, getMessage(e), e.getLocalizedMessage()));
		}
		return "";
	}

	String getMessage(Throwable e) {
		String m = "";
		if (e != null && e.getCause() != null) {
			m = getMessage(e.getCause());
		}
		return m + e.getMessage();
	}

	public HtmlPanelGroup getDataTableGroup() {
		if (dataTableGroup != null)
			dataTableGroup.getChildren().clear();
		return dataTableGroup;
	}

	public void setDataTableGroup(HtmlPanelGroup panel) {
		this.dataTableGroup = panel;
	}

	private HtmlDataTable populateDataTable(List<List<?>> list) {
		HtmlDataTable dynamicDataTable = new HtmlDataTable();
		dynamicDataTable.setId("dynamicDataTable_" + System.currentTimeMillis());
		dynamicDataTable.setValueExpression("value", createValueExpression("#{downloadBean.results}", List.class));
		dynamicDataTable.setVar("line");
		dynamicDataTable.setStyleClass("table table-striped table-bordered table-hover");

		String[] headers = configuration != null ? configuration.toHeaders(list.get(0).size()) : null;
		// Iterate over columns.
		for (int idx = 0; idx < list.get(0).size(); idx++) {

			// Create <h:column>.
			HtmlColumn column = new HtmlColumn();
			dynamicDataTable.getChildren().add(column);

			// Create <h:outputText value="dynamicHeaders[i]"> for <f:facet
			// name="header"> of column.
			HtmlOutputText header = new HtmlOutputText();
			header.setValue("" + (headers != null ? headers[idx] : idx));
			column.setHeader(header);

			// Create <h:outputText value="#{dynamicItem[" + i + "]}"> for the
			// body of column.
			HtmlOutputText output = new HtmlOutputText();
			output.setValueExpression("value", createValueExpression("#{line[" + idx + "]}", String.class));
			column.getChildren().add(output);
		}
		return dynamicDataTable;
	}
	// Helpers
	// -----------------------------------------------------------------------------------

	private ValueExpression createValueExpression(String valueExpression, Class<?> valueType) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(),
				valueExpression, valueType);
	}
	
	// Download Results 

	public Configuration getConfiguration() {
		return configuration;
	}
	
	public String downloadResults() {
		try {
			Configuration config = configuration;
			if (config == null) {
				config = new Configuration();
				config.setCkey("Query");
				config.setCvalue(query);
			}
			ExternalContext ectx = setHeaders(config.getCkey());
			try (OutputStream os = ectx.getResponseOutputStream()) {
				Workbook wb = generateResultsWorkbook(config, results);
				wb.write(os);
			}
			FacesContext.getCurrentInstance().responseComplete();
	
		} catch (Exception e) {
			log.info("executeQuery: " + e, e);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), null));
		}
		return null;
	}
	
	private HSSFWorkbook generateResultsWorkbook(Configuration config, List<List<?>> results) {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(config.getCkey()+"_"+new DateTime().toString(SafeDatePattern));
		CellStyle red = wb.createCellStyle();
		
		if (results != null && results.size() > 0) {
			int rCount = 0;
			HSSFRow row = sheet.createRow(rCount++);
			
			String[] headers = config.toHeaders(results.get(0).size());
			for (int i = 0; i < headers.length; i++) {
				HSSFCell c = row.createCell(i);
				c.setCellValue(headers[i]);
			}

			for (List<?> resultRow : results) {
				row = sheet.createRow(rCount++);
	
				int cCount = 0;
				for (Object o : resultRow) {
					row.createCell(cCount++).setCellValue(o!=null ? ""+o : "");
				}
			}
		}
		return wb;
	}
}
