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
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.jboss.logging.Logger;
import org.joda.time.DateTime;

import com.google.common.net.HttpHeaders;

import at.tfr.pfad.ConfigurationType;
import at.tfr.pfad.Role;
import at.tfr.pfad.dao.ConfigurationRepository;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Configuration;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Squad;
import at.tfr.pfad.processing.RegistrationDataGenerator;
import at.tfr.pfad.processing.RegistrationDataGenerator.DataStructure;
import at.tfr.pfad.processing.RegistrationDataGenerator.RegConfig;
import at.tfr.pfad.util.SessionBean;

@Named
@SessionScoped
@Stateful
public class DownloadBean implements Serializable {

	private Logger log = Logger.getLogger(getClass());
	
	@Inject
	private SessionBean sessionBean;
	@Inject
	private SquadBean squadBean;
	@Inject
	private SquadRepository squadRepo;
	@Inject
	private ConfigurationRepository configRepo;
	@Inject
	private EntityManager em;
	@Inject
	private RegistrationDataGenerator regDataGenerator;
	private Configuration configuration;
	private boolean updateRegistered;
	private boolean notRegisteredOnly;
	private String query;
	private boolean nativeQuery;
	private ListDataModel<List<?>> results = new ListDataModel<>(new ArrayList<>());
	private HtmlPanelGroup dataTableGroup;
	public static final String SafeDatePattern = "yyyy.MM.dd_HHmm";

	public String downloadVorRegistrierung() throws Exception {
		Collection<Member> leaders = squadRepo.findLeaders();
		Predicate<Member> filter = 
				m -> (leaders.contains(m) || m.getFunktionen().stream().anyMatch(f -> Boolean.TRUE.equals(f.getExportReg())));
		return downloadData(new RegConfig().asVorRegistrierung(), filter);
	}

	public String downloadRegistrierung() throws Exception {
		return downloadData(new RegConfig().withUpdateRegistered(updateRegistered), null);
	}

	public String downloadNachRegistrierung() throws Exception {
		return downloadData(new RegConfig().withUpdateRegistered(updateRegistered).notRegistered(notRegisteredOnly), null);
	}

	public String downloadAll() throws Exception {
		return downloadData(new RegConfig().withLocal(), null, sessionBean.getSquad());
	}

	public String downloadAllWithBookings() throws Exception {
		return downloadData(new RegConfig().withLocal().withBookings(), x->true, sessionBean.getSquad());
	}

	public String downloadSquad(Squad squad) throws Exception {
		return downloadData(new RegConfig().withLocal(), x ->true, squad);
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

	public boolean isUpdateRegistered() {
		return updateRegistered;
	}
	
	public void setUpdateRegistered(boolean updateRegistered) {
		this.updateRegistered = updateRegistered;
	}
	
	public boolean isNotRegisteredOnly() {
		return notRegisteredOnly;
	}
	
	public void setNotRegisteredOnly(boolean notRegisteredOnly) {
		this.notRegisteredOnly = notRegisteredOnly;
	}
	
	public String downloadData(RegConfig config, Predicate<Member> filter, Squad... squads) throws Exception {
		try {

			if (!isDownloadAllowed(squads))
				throw new SecurityException(
						"user may not download: " + sessionBean.getUserSession().getCallerPrincipal());

			ExternalContext ectx = setHeaders("Export");
			try (OutputStream os = ectx.getResponseOutputStream()) {
				HSSFWorkbook wb = regDataGenerator.generateData(config, filter, squads);
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

	public ListDataModel<List<?>> getResults() {
		return results;
	}

	public List<List<?>> getResultList() {
		return results != null ? (List<List<?>>)results.getWrappedData() : null;
	}

	public List<Integer> getResultIndexes() {
		return IntStream.range(0, results.getRowCount()).boxed().collect(Collectors.toList());
	}

	public List<Configuration> getQueries() {
		return configRepo.findAll().stream()
				.filter(c -> c.getCkey() != null && (c.getType() == ConfigurationType.query || c.getType() == ConfigurationType.nativeQuery))
				.filter(q -> sessionBean.isAdmin() || Role.none.equals(q.getRole())
						|| sessionBean.getUserSession().isCallerInRole(q.getRole().name()))
				.sorted((x,y) -> x.getCkey().compareTo(y.getCkey()))
				.collect(Collectors.toList());
	}

	public void executeQuery(Long configurationId) {
		try {
			Optional<Configuration> confOpt = getQueries().stream().filter(q -> configurationId.equals(q.getId()))
					.findFirst();
			if (confOpt.isPresent()) {
				configuration = confOpt.get();
				query = configuration.getCvalue();
				nativeQuery = ConfigurationType.nativeQuery.equals(configuration.getType());
				executeQuery();
			}
		} catch (Exception e) {
			log.info("executeQuery: " + e, e);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), null));
		}
	}

	@SuppressWarnings("unchecked")
	public String executeQuery() {
		Query q = null;
		if (dataTableGroup != null) {
			dataTableGroup.getChildren().clear();
		}
		if (configuration != null) {
			nativeQuery = ConfigurationType.nativeQuery.equals(configuration.getType());
		}
		try {
			results = new ListDataModel<>(new ArrayList<>());
			List<?> res;
			if (nativeQuery) {
				res = em.createNativeQuery(query).getResultList();
			} else {
				q = em.createQuery(query);
				res = q.getResultList();
			}
			if (res.size() > 0) {
				if (res.get(0) instanceof Tuple) {
					results.setWrappedData(((List<Tuple>) res).stream().map(r -> r.getElements()).collect(Collectors.toList()));
				} else if (res.get(0) instanceof Object[]) {
					results.setWrappedData(res.stream().map(r -> Arrays.asList((Object[]) r)).collect(Collectors.toList()));
				} else {
					results.setWrappedData(res.stream().map(r -> Arrays.asList(new Object[] { r })).collect(Collectors.toList()));
				}
			}
			if (dataTableGroup != null && results != null && results.getRowCount() > 0) {
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

	private HtmlDataTable populateDataTable(ListDataModel<List<?>> dataModel) {
		HtmlDataTable dynamicDataTable = new HtmlDataTable();
		dynamicDataTable.setId("dynamicDataTable_" + System.currentTimeMillis());
		dynamicDataTable.setValueExpression("value", createValueExpression("#{downloadBean.results}", dataModel.getClass()));
		dynamicDataTable.setVar("line");
		dynamicDataTable.setStyleClass("table table-striped table-bordered table-hover");
		
		List<?> firstRow = ((List<List<?>>)dataModel.getWrappedData()).get(0);

		String[] headers = configuration != null ? configuration.toHeaders(firstRow.size()) : null;
		
		if (dataModel.getRowCount() > 0) {
			// Create <h:column>.
			HtmlColumn column = new HtmlColumn();
			dynamicDataTable.getChildren().add(column);

			// name="header"> of column.
			HtmlOutputText header = new HtmlOutputText();
			header.setValue("Nr");
			column.setHeader(header);

			// body of column.
			HtmlOutputText output = new HtmlOutputText();
			output.setValueExpression("value", createValueExpression("#{downloadBean.results.rowIndex+1}", Integer.class));
			column.getChildren().add(output);
		}
		
		// Iterate over columns.
		for (int idx = 0; idx < firstRow.size(); idx++) {

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
				Workbook wb = generateResultsWorkbook(config, getResultList());
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
