package at.tfr.pfad.view;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;

import at.tfr.pfad.SquadType;
import at.tfr.pfad.model.Function;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Squad;
import at.tfr.pfad.view.DownloadBean.ValidationResult;

@RunWith(CdiTestRunner.class)
public class TestDownloadBean {

	private DownloadBean db = new DownloadBean();
	private Collection<Member> leaders = new ArrayList<Member>();
	
	@Test
	public void testValidationVollzahler() throws Exception {
		
		Member vollz = new Member();
		vollz.setId(1L); // for equals() test
		Member m = new Member();
		m.setId(2L);
		m.setAktiv(true);
		m.setVollzahler(vollz);
		
		List<ValidationResult> vr = db.validate(m, "", leaders);
		assertTrue("Vollzahler auf Inaktiv", vr.stream().anyMatch(r -> r.message.contains("Vollzahler")));
		
		vollz.setAktivExtern(true);
		vr = db.validate(m, "", leaders);
		assertTrue("Vollzahler auf AktivExtern", !vr.stream().anyMatch(r -> r.message.contains("Vollzahler")));
		
		vollz.setAktiv(true);
		vollz.setAktivExtern(false);
		vr = db.validate(m, "", leaders);
		assertTrue("Vollzahler auf Aktiv", !vr.stream().anyMatch(r -> r.message.contains("Vollzahler")));
		
	}

	@Test
	public void testValidationDatum() throws Exception {
		
		Member m = new Member();
		
		// No checks for Inaktiv
		List<ValidationResult> vr = db.validate(m, "", leaders);
		assertFalse("Datum Unvollständig", vr.stream().anyMatch(r -> r.message.contains(DownloadBean.GEB_UNVOLL)));

		assertTrue("exportable", db.isNotGrinsExportable(m, leaders));
		
		Function f = new Function();
		f.setId(1L);
		f.setExportReg(true);
		m.getFunktionen().add(f);

		assertFalse("exportable", db.isNotGrinsExportable(m, leaders));
		
		vr = db.validate(m, "", leaders);
		assertTrue("Datum Unvollständig", vr.stream().anyMatch(r -> r.message.contains(DownloadBean.GEB_UNVOLL)));
		
		m.setAktiv(true);
		vr = db.validate(m, "", leaders);
		assertTrue("Datum Unvollständig", vr.stream().anyMatch(r -> r.message.contains(DownloadBean.GEB_UNVOLL)));

		m.setGebJahr(1900);
		vr = db.validate(m, "", leaders);
		assertTrue("Datum Unvollständig", vr.stream().anyMatch(r -> r.message.contains(DownloadBean.GEB_UNVOLL)));

		m.setGebMonat(1);
		vr = db.validate(m, "", leaders);
		assertTrue("Datum Unvollständig", vr.stream().anyMatch(r -> r.message.contains(DownloadBean.GEB_UNVOLL)));
		
		m.setGebTag(1);
		vr = db.validate(m, "", leaders);
		assertFalse("Datum Unvollständig", vr.stream().anyMatch(r -> r.message.contains(DownloadBean.GEB_UNVOLL)));
		
}
	
	@Test
	public void testValidationStufe() throws Exception {
		
		Member m = new Member();
		
		m.setGebJahr(1900);
		m.setGebMonat(1);
		m.setGebTag(1);

		int currentYear = new DateTime().getYear();
		SquadType st = SquadType.CAEX;

		Squad trupp = new Squad();
		trupp.setType(st);
		m.setTrupp(trupp);
		
		m.setGebJahr(1900);
		List<ValidationResult> vr = db.validate(m, "", leaders);
		assertTrue(DownloadBean.ZU_ALT, vr.stream().anyMatch(r -> r.message.contains(DownloadBean.ZU_ALT)));
		
		m.setGebJahr(currentYear-st.getMax()-2);
		vr = db.validate(m, "", leaders);
		assertTrue(DownloadBean.ZU_ALT, vr.stream().anyMatch(r -> r.message.contains(DownloadBean.ZU_ALT)));
		
		m.setGebJahr(currentYear-st.getMin()+2);
		vr = db.validate(m, "", leaders);
		assertTrue(DownloadBean.ZU_JUNG, vr.stream().anyMatch(r -> r.message.contains(DownloadBean.ZU_JUNG)));

		// Just OK

		m.setGebJahr(currentYear-st.getMax()-1);
		vr = db.validate(m, "", leaders);
		assertFalse(DownloadBean.ZU_ALT, vr.stream().anyMatch(r -> r.message.contains(DownloadBean.ZU_ALT)));
		
		m.setGebJahr(currentYear-st.getMin()+1);
		vr = db.validate(m, "", leaders);
		assertFalse(DownloadBean.ZU_JUNG, vr.stream().anyMatch(r -> r.message.contains(DownloadBean.ZU_JUNG)));
	}

	@Test
	public void testValidationTruppVerein() throws Exception {
		
		Member m = new Member();
		m.setAktiv(true);
		
		List<ValidationResult> vr = db.validate(m, "", leaders);
		assertTrue(DownloadBean.KEIN_TRUPP_FUNKTION, vr.stream().anyMatch(r -> r.message.contains(DownloadBean.KEIN_TRUPP_FUNKTION)));

		m.setAktiv(false);
		
		vr = db.validate(m, "", leaders);
		assertFalse(DownloadBean.KEIN_TRUPP_FUNKTION, vr.stream().anyMatch(r -> r.message.contains(DownloadBean.KEIN_TRUPP_FUNKTION)));

		Squad trupp = new Squad();
		trupp.setType(SquadType.GUSP);
		m.setTrupp(trupp);

		vr = db.validate(m, "", leaders);
		assertTrue(DownloadBean.INAKTIV_IM_TRUPP, vr.stream().anyMatch(r -> r.message.contains(DownloadBean.INAKTIV_IM_TRUPP)));

	}
}
