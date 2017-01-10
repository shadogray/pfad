package at.tfr.pfad;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import at.tfr.pfad.dao.ActivityRepository;
import at.tfr.pfad.dao.BookingRepository;
import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Squad;
import at.tfr.pfad.processing.ProcessData;
import at.tfr.pfad.processing.ProcessExcelPayments;

@RunWith(CdiTestRunner.class)
public class TestExcelPaymentImport {

	final String excelFile = "./src/test/data/ImportPayments.xlsx";
	@Inject
	private ProcessExcelPayments procPayments;
	@Inject
	private ActivityRepository activityRepo;
	@Inject
	private MemberRepository memberRepo;
	@Inject
	private BookingRepository bookingRepo;
	@Inject
	private SquadRepository squadRepo;

	@Before
	public void init() throws Exception {
		Activity a = createActivity();
		Squad s = createSquad();
		
		Member m = createMember("Name", "Vorname", s);
		
		Member m2 = createMember("NameZwei", "VornameZwei", s);
		
		createBooking(a, m);
		createBooking(a, m2);
	}

	@Test
	public void testExcelImport() throws Exception {
		
		Activity activity = activityRepo.findAll().iterator().next();
		
		ProcessData data = new ProcessData(activity);
		data.setCreatePayment(true);
		data.setAccontoGrades(new Double[]{50D, 100D});
		Path xmlFile = Paths.get(excelFile);
		System.out.println("Reading: "+xmlFile);
		
		try (InputStream is = Files.newInputStream(xmlFile); 
				XSSFWorkbook wb = (XSSFWorkbook)WorkbookFactory.create(is)) {
		
			for (Sheet sheet : wb) {
				int lastrow = sheet.getLastRowNum();
				for (Row row : sheet) {
					row = procPayments.processRow((XSSFRow)row, data);
					if (row.getRowNum() > lastrow) {
						break;
					}
				}
			}
			
			Path outFile = xmlFile.resolveSibling(xmlFile.getFileName()+".out.xlsx");
			System.out.println("Writing: "+outFile);
			wb.write(Files.newOutputStream(outFile));
		}
	}
	
	
	private Booking createBooking(Activity a, Member m) {
		Booking b = new Booking();
		b.setMember(m);
		b.setActivity(a);
		b.setStatus(BookingStatus.created);
		return bookingRepo.save(b);
	}

	private Squad createSquad() {
		Squad s = new Squad();
		s.setName("TEST");
		s.setType(SquadType.CAEX);
		s = squadRepo.save(s);
		return s;
	}

	private Activity createActivity() {
		Activity a = new Activity();
		a.setAmount(70.0F);
		a.setName("TEST");
		a.setType(ActivityType.Membership);
		a.setStatus(ActivityStatus.planned);
		a = activityRepo.save(a);
		return a;
	}

	private Member createMember(String name, String vorname, Squad s) {
		Member m = new Member();
		m.setName(name);
		m.setVorname(vorname);
		m.setGebJahr(1990);
		m.setGebMonat(1);
		m.setGebTag(1);
		m.setTrupp(s);
		m = memberRepo.save(m);
		return m;
	}
	
}
