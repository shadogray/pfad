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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.ejb.SessionContext;
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
import org.jboss.logging.Logger;
import org.joda.time.DateTime;

import com.google.common.net.HttpHeaders;

import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Squad;

@Named
@SessionScoped
public class DownloadBean implements Serializable {

	private Logger log = Logger.getLogger(getClass());
	
	enum HeaderRegistrierung {
		BVKey, GruppenSchlüssel, PersonenKey, Titel, Name, Vorname, Anrede, GebTag, GebMonat, GebJahr, Straße, PLZ, Ort, Geschlecht, Aktiv, Vollzahler, Email, Telefon, Funktionen, Trupp, OK
	}

	enum HeaderLocal {
		Religion, FunktionenBaden, Trail, Gilde, AltER, InfoMail, Mitarbeit, Eltern, Kinder, KinderTrupps
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
	@Inject
	private EntityManager em;
	private String query;
	private boolean nativeQuery;
	private List<List<?>> results = new ArrayList<>();
	private HtmlPanelGroup dataTableGroup;

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
		CellStyle red = wb.createCellStyle();
		red.setFillForegroundColor(HSSFColor.RED.index);
		red.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		int rCount = 0;
		HSSFRow row = sheet.createRow(rCount++);

		List<String> headers = Arrays.asList(HeaderRegistrierung.values()).stream().map(h -> h.name())
				.collect(Collectors.toList());
		if (withLocal) {
			headers.addAll(
					Arrays.asList(HeaderLocal.values()).stream().map(h -> h.name()).collect(Collectors.toList()));
		}

		for (int i = 0; i < headers.size(); i++) {
			String h = transformHeaders(headers, i);
			HSSFCell c = row.createCell(i);
			c.setCellValue(h);
		}

		Collection<Member> leaders = squadRepo.findLeaders();

		for (Member m : getMembers()) {

			if (!withLocal && !(m.isAktiv() || leaders.contains(m)
					|| m.getFunktionen().stream().filter(f -> f.isExportReg()).count() > 0))
				continue;
			
			if (squads != null && squads.length > 0) {
				if (!Stream.of(squads).anyMatch(s->s.equals(m.getTrupp()))) {
					continue;
				}
			}

			ValidationResult vr = validate(m);

			row = sheet.createRow(rCount++);

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
			row.createCell(cCount++).setCellValue(m.getGeschlecht() != null ? m.getGeschlecht().name() : "");
			row.createCell(cCount++).setCellValue(m.isAktiv() ? "J" : "N");
			row.createCell(cCount++).setCellValue(m.getVollzahler() != null ? m.getVollzahler().getBVKey() : "");
			row.createCell(cCount++).setCellValue(m.getEmail());
			row.createCell(cCount++).setCellValue(m.getTelefon());
			row.createCell(cCount++).setCellValue(getFunktionen(m));
			row.createCell(cCount++).setCellValue(m.getTrupp() != null ? m.getTrupp().getName() : "");
			
			HSSFCell ok = row.createCell(cCount++);
			ok.setCellValue(vr.message);
			if (!vr.valid) {
				ok.setCellStyle(red);
			}

			// and Local Data
			if (withLocal) { // Religion, FunktionenBaden, Trail, Gilde, AltER
				row.createCell(cCount++).setCellValue(m.getReligion());
				row.createCell(cCount++).setCellValue("");
				row.createCell(cCount++).setCellValue(m.isTrail() ? "X" : "");
				row.createCell(cCount++).setCellValue(m.isGilde() ? "X" : "");
				row.createCell(cCount++).setCellValue(m.isAltER() ? "X" : "");
				row.createCell(cCount++).setCellValue(m.isInfoMail() ? "X" : "");
				row.createCell(cCount++).setCellValue(m.isSupport() ? "X" : "");
				// Eltern, Kinder, KinderTrupps
				row.createCell(cCount++).setCellValue(!m.getSiblings().isEmpty() ? "X" : "");
				row.createCell(cCount++).setCellValue(m.getSiblings().stream().map(s->s.toString()).collect(Collectors.joining(",")));
				row.createCell(cCount++).setCellValue(m.getSiblings().stream().map(s->s.getTrupp()!=null ? s.getTrupp().getName() : "").collect(Collectors.joining(",")));
			}

		}
		return wb;
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
		}
		return h;
	}

	private List<Member> getMembers() {
		List<Member> members = membRepo.findAll().stream().sorted().collect(Collectors.toList());
		SessionContext ctx = memberBean.getSessionContext();
		if (ctx.isCallerInRole(Roles.admin.name()) || ctx.isCallerInRole(Roles.gruppe.name()))
			return members;
		if (ctx.isCallerInRole(Roles.leiter.name())) {
			List<Squad> squads = squadRepo.findByName(ctx.getCallerPrincipal().getName());
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
		List<String> functions = new ArrayList<>();
		if (m.getTrupp() != null) {
			functions.add(m.getTrupp().getType().getKey(m.getGeschlecht()));
		}
		
		List<String> toLead = squadRepo.findByLeaderFemaleEqualOrLeaderMaleEqual(m).stream()
				.map(s -> "AS" + s.getType().getKey(m.getGeschlecht())).collect(Collectors.toList());
		functions.addAll(toLead);
		
		List<String> toAss = squadRepo.findByAssistant(m).stream()
				.map(s -> "AS" + s.getType().getKey(m.getGeschlecht())).collect(Collectors.toList());
		functions.addAll(toAss);
		
		if (!m.getFunktionen().isEmpty()) {
			functions.addAll(m.getFunktionen().stream().map(f -> f.getKey()).collect(Collectors.toList()));
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(functions.stream().distinct().collect(Collectors.joining(",")));
		return sb.toString().trim();
	}

	public ValidationResult validate(Member member) {
		if (member.isAktiv() && member.getVollzahler() != null
				&& !member.equals(member.getVollzahler()) 
				&&	!(member.getVollzahler().isAktiv() || member.getVollzahler().isAktivExtern())) {
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
					results = ((List<Tuple>)res).stream().map(r->r.getElements()).collect(Collectors.toList());
				} else if (res.get(0) instanceof Object[]) {
					results = res.stream().map(r->Arrays.asList((Object[])r)).collect(Collectors.toList());
				} else {
					results = res.stream().map(r->Arrays.asList(new Object[]{r})).collect(Collectors.toList());
				}
			}
			if (dataTableGroup != null && results != null && results.size() > 0) {
				dataTableGroup.getChildren().add(populateDataTable(results));
				dataTableGroup = null;
			}
		} catch (Exception e) {
			log.info("cannot execute: "+q+" : "+e, e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
					getMessage(e), e.getLocalizedMessage()));
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
		if (dataTableGroup != null && (results == null || results.isEmpty()))
			dataTableGroup.getChildren().clear();
		return dataTableGroup;
	}
	
	public void setDataTableGroup(HtmlPanelGroup panel) {
		this.dataTableGroup = panel;
	}
	
	private HtmlDataTable populateDataTable(List<List<?>> list) {
	    HtmlDataTable dynamicDataTable = new HtmlDataTable();
	    dynamicDataTable.setId("dynamicDataTable_"+System.currentTimeMillis());
        dynamicDataTable.setValueExpression("value",createValueExpression("#{downloadBean.results}", List.class));
        dynamicDataTable.setVar("line");
        dynamicDataTable.setStyleClass("table table-striped table-bordered table-hover");

        // Iterate over columns.
        for (int idx = 0; idx < list.get(0).size(); idx++) {

            // Create <h:column>.
            HtmlColumn column = new HtmlColumn();
            dynamicDataTable.getChildren().add(column);

            // Create <h:outputText value="dynamicHeaders[i]"> for <f:facet name="header"> of column.
            HtmlOutputText header = new HtmlOutputText();
            header.setValue(""+idx);
            column.setHeader(header);

            // Create <h:outputText value="#{dynamicItem[" + i + "]}"> for the body of column.
            HtmlOutputText output = new HtmlOutputText();
            output.setValueExpression("value",
                createValueExpression("#{line[" + idx + "]}", String.class));
            column.getChildren().add(output);
        }
        return dynamicDataTable;
	}
    // Helpers -----------------------------------------------------------------------------------

    private ValueExpression createValueExpression(String valueExpression, Class<?> valueType) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        return facesContext.getApplication().getExpressionFactory().createValueExpression(
            facesContext.getELContext(), valueExpression, valueType);
    }}
