package at.tfr.pfad.view;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.logging.Logger;

import at.tfr.pfad.model.Activity;
import at.tfr.pfad.processing.ProcessData;
import at.tfr.pfad.processing.ProcessExcelPayments;

@Named
@ViewScoped
@Stateful
public class PaymentImportBean implements Serializable {

	private Logger log = Logger.getLogger(getClass());
	
	@Inject
	private ProcessExcelPayments processor;
	
	private Part fileContent;
	private byte[] resultContent;
	private String amountGrades = StringUtils.join(new ProcessData().getAmountGrades(), ",");
	private String accontoGrades;
	private Activity activity;
	
	public void test() {
		process(false);
	}
	
	public void execute() {
		process(true);
	}
	
	public void process(boolean execute) {
		
		try {
		
			ProcessData data = new ProcessData(activity);
			data.setCreatePayment(execute);
			if (StringUtils.isNoneBlank(amountGrades)) {
				data.setAmountGrades(parse(amountGrades));
			}
			if (StringUtils.isNoneBlank(accontoGrades)) {
				data.setAmountGrades(parse(accontoGrades));
			}
			
			try (XSSFWorkbook wb = (XSSFWorkbook)WorkbookFactory.create(fileContent.getInputStream())) {
			
				for (Sheet sheet : wb) {
					int lastrow = sheet.getLastRowNum();
					for (Row row : sheet) {
						row = processor.processRow((XSSFRow)row, data);
						if (row.getRowNum() > lastrow) {
							break;
						}
					}
				}
				
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				wb.write(os);
				wb.close();
				resultContent = os.toByteArray();
			}
	
			
		} catch (Exception e) {
			log.info("cannot process: "+e, e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to process: "+e, ""));
		}
	}
	
	public void download() throws IOException {
		DownloadBean.setHeaders("ImportPayments");
		OutputStream os = FacesContext.getCurrentInstance().getExternalContext().getResponseOutputStream();
		os.write(resultContent);
		FacesContext.getCurrentInstance().responseComplete();
	}

	private Double[] parse(String grades) {
		return Stream.of(grades.split(",")).map(s->Double.valueOf(s))
				.collect(Collectors.toList()).toArray(new Double[]{});
	}

	public Part getFileContent() {
		return fileContent;
	}
	
	public void setFileContent(Part fileContent) {
		this.fileContent = fileContent;
	}
	
	public byte[] getResultContent() {
		return resultContent;
	}
	
	public void setResultContent(byte[] resultContent) {
		this.resultContent = resultContent;
	}
	
	public String getAmountGrades() {
		return amountGrades;
	}

	public void setAmountGrades(String amountGrades) {
		this.amountGrades = amountGrades;
	}

	public String getAccontoGrades() {
		return accontoGrades;
	}

	public void setAccontoGrades(String accontoGrades) {
		this.accontoGrades = accontoGrades;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	
}
