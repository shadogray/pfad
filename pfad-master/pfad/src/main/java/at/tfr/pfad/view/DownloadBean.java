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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.ListDataModel;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;
import javax.ws.rs.core.HttpHeaders;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.logging.Logger;
import org.joda.time.DateTime;
import org.omnifaces.util.Messages;

import at.tfr.pfad.ConfigurationType;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Configuration;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Squad;
import at.tfr.pfad.processing.RegistrationDataGenerator;
import at.tfr.pfad.processing.RegistrationDataGenerator.DataStructure;
import at.tfr.pfad.processing.RegistrationDataGenerator.RegConfig;
import at.tfr.pfad.util.ColumnModel;
import at.tfr.pfad.util.QueryExecutor;
import at.tfr.pfad.util.SessionBean;
import at.tfr.pfad.util.TemplateUtils;

@Named
@ViewScoped
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
	private QueryExecutor qExec;
	@Inject
	private RegistrationDataGenerator regDataGenerator;
	@Inject
	private TemplateUtils templateUtils;
	private Configuration configuration = new Configuration().withCkey("undef").withUiName("undef");
	private boolean updateRegistered;
	private boolean notRegisteredOnly;
	private List<List<Entry<String,Object>>> results = Collections.emptyList();
	private ListDataModel<List<Entry<String,Object>>> resultModel = new ListDataModel<>(new ArrayList<>());
	private final List<ColumnModel> columns = new ArrayList<>();
	private final List<String> columnHeaders = new ArrayList<>();
	public static final String SafeDatePattern = "yyyy.MM.dd_HHmm";
	
	private final Map<String,Object> beans = new HashMap<>();
	private boolean activeOnly = true;
	private Activity payedActivity;
	public List<Configuration> queries = Collections.emptyList();
	
	@PostConstruct
	public void init() {
		Squad responsibleFor = sessionBean.isResponsibleFor();
		beans.put("truppName", responsibleFor != null ? responsibleFor.getName() : "");
		beans.put("truppId", responsibleFor != null ? responsibleFor.getId() : "0");
		beans.put("trupp", sessionBean.getSquad());
		beans.put("user", sessionBean.getUser());
		initQueries();
	}

	public String downloadVorRegistrierung() throws Exception {
		Collection<Member> leaders = squadRepo.findLeaders();
		Predicate<Member> filter = 
				m -> (leaders.contains(m) || m.getFunktionen().stream().anyMatch(f -> f.isExportReg()));
		return downloadData(getRegConfig().asVorRegistrierung(), filter);
	}

	public String downloadRegistrierung() throws Exception {
		return downloadData(getRegConfig().withUpdateRegistered(updateRegistered), null);
	}

	public String downloadNachRegistrierung() throws Exception {
		return downloadData(getRegConfig().withUpdateRegistered(updateRegistered).notRegistered(notRegisteredOnly), null);
	}

	public String downloadAll() throws Exception {
		return downloadData(getRegConfig().withLocal(), null, sessionBean.getSquad());
	}

	public String downloadAllWithBookings() throws Exception {
		return downloadData(getRegConfig().withLocal().withBookings(), x->true, sessionBean.getSquad());
	}

	public String downloadSquad(Squad squad) throws Exception {
		return downloadData(getRegConfig().withLocal(), x ->true, squad);
	}

	private RegConfig getRegConfig() {
		return new RegConfig(payedActivity);
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
	
	public Activity getPayedActivity() {
		return payedActivity;
	}
	
	public void setPayedActivity(Activity payedActivity) {
		this.payedActivity = payedActivity;
	}
	
	public boolean isActiveOnly() {
		return activeOnly;
	}
	
	public void setActiveOnly(boolean activeOnly) {
		this.activeOnly = activeOnly;
	}
	
	public String downloadData(RegConfig config, Predicate<Member> filter, Squad... squads) throws Exception {
		try {

			if (!isDownloadAllowed(squads))
				throw new SecurityException(
						"user may not download: " + sessionBean.getUserSession().getCallerPrincipal());

			ExternalContext ectx = setHeaders("Export");
			try (OutputStream os = ectx.getResponseOutputStream()) {
				XSSFWorkbook wb = regDataGenerator.generateData(config, filter, squads);
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
		DataStructure dataStructure = DataStructure.XLSX;
		String encoding = "UTF8";
		return setHeaders(prefix.replaceAll("[ /\\\\:\\.,\\|\\?\"']+", "_"), dataStructure, encoding);
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
		return configuration.getCvalue();
	}

	public void setQuery(String query) {
		configuration.setCvalue(query);
	}

	public boolean isNativeQuery() {
		return configuration.isNative();
	}

	public void setNativeQuery(boolean nativeQuery) {
		configuration.setNative(nativeQuery);
	}

	public ListDataModel<List<Entry<String,Object>>> getResults() {
		return resultModel;
	}
	
	public List<String> getColumnHeaders() {
		return columnHeaders;
	}
	
	public List<ColumnModel> getColumns() {
		return columns;
	}

	@SuppressWarnings("unchecked")
	public List<List<Entry<String,Object>>> getResultList() {
		return results;
	}

	public List<Configuration> getQueries() {
		return queries;
	}
	
	public void initQueries() {
		queries = sessionBean.getConfig().stream()
				.filter(c -> c.getCkey() != null && (c.getType() == ConfigurationType.query || c.getType() == ConfigurationType.nativeQuery))
				.sorted((x,y) -> x.getCkey().compareTo(y.getCkey()))
				.collect(Collectors.toList());
		queries.forEach(q -> q.setUiName(q.isDownload() ? q.getDownloadName() : q.getCkey()));
		queries.forEach(q -> q.setUiName(templateUtils.replace(q.getUiName(), beans, q.getUiName())));
	}

	public void executeQuery(Long configurationId) {
		try {
			Optional<Configuration> confOpt = getQueries().stream().filter(q -> configurationId.equals(q.getId()))
					.findFirst();
			if (confOpt.isPresent()) {
				executeQuery(confOpt.get());
			}
		} catch (Exception e) {
			log.info("executeQuery: " + e, e);
			Messages.addError(null, e.getLocalizedMessage());
		}
	}

	public void executeQuery(Configuration config) {
		this.configuration = config;
		executeQueryIntern();
	}

	public String executeQuery() {
		try {
			executeQueryIntern();
		} catch (Exception e) {
			log.info("cannot execute: " + configuration + " : " + e, e);
			Messages.addError(null, getMessage(e), e.getLocalizedMessage());
		}
		return "";
	}

	private void executeQueryIntern() {
		results = Collections.emptyList();
		columnHeaders.clear();
		columns.clear();
		resultModel = new ListDataModel<List<Entry<String,Object>>>(new ArrayList<List<Entry<String,Object>>>());

		String replQuery = configuration.getCvalue();
		if (replQuery != null) {
			replQuery = templateUtils.replace(replQuery, beans);
		}
		results = qExec.execute(replQuery, configuration.isNative());
		if (results.size() > 0) {
			resultModel.setWrappedData(results);
			List<String> columnNames = results.get(0).stream().map(Entry::getKey).collect(Collectors.toList());

			if (configuration != null) 
				columnHeaders.addAll(Arrays.asList(configuration.toHeaders(columnNames)));
			for (int i=0; i<columnNames.size(); i++) 
				columns.add(new ColumnModel(columnNames.get(i), 
						columnHeaders.size()>i ? columnHeaders.get(i) : columnNames.get(i), i));
		}
	}

	String getMessage(Throwable e) {
		String m = "";
		if (e != null && e.getCause() != null) {
			m = getMessage(e.getCause());
		}
		return m + e.getMessage();
	}

	// Download Results 
	public Configuration getConfiguration() {
		return configuration;
	}
	
	public String downloadResults() {
		return downloadConfiguration(configuration);
	}

	public String downloadConfiguration(Configuration config) {
		try {
			executeQuery(config);
			ExternalContext ectx = setHeaders(config.getUiName());
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
	
	private XSSFWorkbook generateResultsWorkbook(Configuration config, List<List<Entry<String,Object>>> results) {
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet(config.getCkey()+"_"+new DateTime().toString(SafeDatePattern));
		CellStyle red = wb.createCellStyle();
		
		if (results != null && results.size() > 0) {
			int rCount = 0;
			XSSFRow row = sheet.createRow(rCount++);
			
			String[] headers = config.toHeaders(results.get(0).stream().map(Entry::getKey).collect(Collectors.toList()));
			for (int i = 0; i < headers.length; i++) {
				XSSFCell c = row.createCell(i);
				c.setCellValue(headers[i]);
			}

			for (List<Entry<String,Object>> resultRow : results) {
				row = sheet.createRow(rCount++);
	
				int cCount = 0;
				for (Entry<String,Object> e : resultRow) {
					row.createCell(cCount++).setCellValue(e.getValue()!=null ? ""+e.getValue() : "");
				}
			}
		}
		return wb;
	}
	
}
