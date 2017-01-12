package at.tfr.pfad.view;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.logging.Logger;

import at.tfr.pfad.model.Activity;
import at.tfr.pfad.processing.ProcessData;
import at.tfr.pfad.processing.ProcessExcelPayments;
import at.tfr.pfad.view.DownloadBean.DataStructure;

@Named
@ViewScoped
@Stateful
public class PaymentImportBean implements Serializable {

	private Logger log = Logger.getLogger(getClass());
	
	@Inject
	private ProcessExcelPayments processor;

	private Part fileContentPart;
	private byte[] fileContent;
	private String fileName = "ImportPayments";
	private byte[] resultContent;
	private String amountGrades = StringUtils.join(new ProcessData().getAmountGrades(), ",");
	private String accontoGrades;
	private List<Line> results = new ArrayList<>();
	private Activity activity;
	
	public void test() {
		process(false);
	}
	
	public void execute() {
		process(true);
	}
	
	public void process(boolean execute) {
		
		results = new ArrayList<>();
		
		if (activity == null) {
			error("No Actvity selected!");
			return;
		}
		
		if (fileContent == null) {
			error("No File selected!");
			return;
		}
		try {
		
			ProcessData data = new ProcessData(activity);
			data.setCreatePayment(execute);
			if (StringUtils.isNoneBlank(amountGrades)) {
				data.setAmountGrades(parse(amountGrades));
			}
			if (StringUtils.isNoneBlank(accontoGrades)) {
				data.setAmountGrades(parse(accontoGrades));
			}
			
			try (XSSFWorkbook wb = (XSSFWorkbook)WorkbookFactory.create(new ByteArrayInputStream(fileContent))) {
			
				for (Sheet sheet : wb) {
					int lastrow = sheet.getLastRowNum();
					for (Row row : sheet) {
						
						row = processor.processRow((XSSFRow)row, data);
						results.add(new Line(row));
						
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
			error("Failed to process: "+e);
		}
	}

	private void error(String message) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message,""));
	}
	
	public void download() throws IOException {
		DownloadBean.setHeaders(fileName, DataStructure.XLSX);
		OutputStream os = FacesContext.getCurrentInstance().getExternalContext().getResponseOutputStream();
		os.write(resultContent);
		FacesContext.getCurrentInstance().responseComplete();
	}

	private Double[] parse(String grades) {
		return Stream.of(grades.split(",")).map(s->Double.valueOf(s))
				.collect(Collectors.toList()).toArray(new Double[]{});
	}

	public byte[] getFileContent() {
		return fileContent;
	}
	
	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}
	
	public Part getFileContentPart() {
		return fileContentPart;
	}
	
	public void setFileContentPart(Part fileContent) {
		this.fileContent = null;
		this.fileName = null;
		try {
			if (fileContent != null && fileContent.getSize() > 0) {
				this.fileContent = IOUtils.toByteArray(fileContent.getInputStream());
				this.fileName = fileContent.getSubmittedFileName();
			}
		} catch (Exception e) {
			error("Error: "+e);
		}
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public List<Line> getResults() {
		return results;
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

	public static class Line {
		private List<String> values = new ArrayList<>();
		private int VALS = 10;
		
		public Line() {
			for (int i=0; i<VALS; i++) {
				values.add("");
			}
		}
		
		public Line(Row row) {
			for (int i=0; i<VALS; i++) {
				values.add(null);
			}
			for (int i=0; i<VALS; i++) {
				values.set(i, "");
				try {
					Cell cell = row.getCell(i+1);
					if (cell != null) {
						switch (cell.getCellTypeEnum()) {
						case STRING:
							values.set(i, cell.getStringCellValue());
							break;
						case NUMERIC:
							try {
								values.set(i, ProcessExcelPayments.sdf.format(cell.getDateCellValue()));
							} catch (Exception e) {
								values.set(i, ""+cell.getNumericCellValue());
							}
							break;
						case BOOLEAN:
							values.set(i, ""+cell.getBooleanCellValue());
							break;
						case FORMULA:
							values.set(i, ""+cell.getCellFormula());
							break;
						case BLANK:
						default:
							break;
						}
					}
				} catch (Exception e){}
			}
		}

		public String getVal0() {
			return read(0);
		}

		public void setVal0(String val) {
			write(0, val);
		}

		public String getVal1() {
			return read(1);
		}

		public void setVal1(String val) {
			write(1, val);
		}

		public String getVal2() {
			return read(2);
		}

		public void setVal2(String val) {
			write(2, val);
		}

		public String getVal3() {
			return read(3);
		}

		public void setVal3(String val) {
			write(3, val);
		}

		public String getVal4() {
			return read(4);
		}

		public void setVal4(String val) {
			write(4, val);
		}

		public String getVal5() {
			return read(5);
		}

		public void setVal5(String val) {
			write(5, val);
		}

		public String getVal6() {
			return read(6);
		}

		public void setVal6(String val) {
			write(6, val);
		}

		public String getVal7() {
			return read(7);
		}

		public void setVal7(String val) {
			write(7, val);
		}

		public String getVal8() {
			return read(8);
		}

		public void setVal8(String val) {
			write(8, val);
		}

		public String getVal9() {
			return read(9);
		}

		public void setVal9(String val) {
			write(9, val);
		}

		private String read(int idx) {
			if (values.size() > idx) {
				return values.get(idx);
			}
			return null;
		}

		private String write(int idx, String value) {
			if (values.size() > idx) {
				return values.set(idx, value);
			}
			return null;
		}

	}
	
}
