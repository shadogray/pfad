/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

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
import at.tfr.pfad.view.validator.MemberValidator;
import at.tfr.pfad.view.validator.ValidationResult;

@RunWith(CdiTestRunner.class)
public class TestDownloadBean {

	private DownloadBean db = new DownloadBean();
	private Collection<Member> leaders = new ArrayList<Member>();
	private MemberValidator mv = new MemberValidator();
	
	@Test
	public void testValidationVollzahler() throws Exception {
		
		Member vollz = new Member();
		vollz.setId(1L); // for equals() test
		Member m = new Member();
		m.setId(2L);
		m.setAktiv(true);
		m.setVollzahler(vollz);
		
		List<ValidationResult> vr = mv.validate(m, "", leaders);
		assertTrue("Vollzahler auf Inaktiv", vr.stream().anyMatch(r -> r.getMessage().contains("Vollzahler")));
		
		vollz.setAktivExtern(true);
		vr = mv.validate(m, "", leaders);
		assertTrue("Vollzahler auf AktivExtern", !vr.stream().anyMatch(r -> r.getMessage().contains("Vollzahler")));
		
		vollz.setAktiv(true);
		vollz.setAktivExtern(false);
		vr = mv.validate(m, "", leaders);
		assertTrue("Vollzahler auf Aktiv", !vr.stream().anyMatch(r -> r.getMessage().contains("Vollzahler")));
		
	}

	@Test
	public void testValidationDatum() throws Exception {
		
		Member m = new Member();
		
		// No checks for Inaktiv
		List<ValidationResult> vr = mv.validate(m, "", leaders);
		assertFalse("Datum Unvollständig", vr.stream().anyMatch(r -> r.getMessage().contains(MemberValidator.GEB_UNVOLL)));

		assertTrue("exportable", mv.isNotGrinsExportable(m, leaders));
		
		Function f = new Function();
		f.setId(1L);
		f.setExportReg(true);
		m.getFunktionen().add(f);

		assertFalse("exportable", mv.isNotGrinsExportable(m, leaders));
		
		vr = mv.validate(m, "", leaders);
		assertTrue("Datum Unvollständig", vr.stream().anyMatch(r -> r.getMessage().contains(MemberValidator.GEB_UNVOLL)));
		
		m.setAktiv(true);
		vr = mv.validate(m, "", leaders);
		assertTrue("Datum Unvollständig", vr.stream().anyMatch(r -> r.getMessage().contains(MemberValidator.GEB_UNVOLL)));

		m.setGebJahr(1900);
		vr = mv.validate(m, "", leaders);
		assertTrue("Datum Unvollständig", vr.stream().anyMatch(r -> r.getMessage().contains(MemberValidator.GEB_UNVOLL)));

		m.setGebMonat(1);
		vr = mv.validate(m, "", leaders);
		assertTrue("Datum Unvollständig", vr.stream().anyMatch(r -> r.getMessage().contains(MemberValidator.GEB_UNVOLL)));
		
		m.setGebTag(1);
		vr = mv.validate(m, "", leaders);
		assertFalse("Datum Unvollständig", vr.stream().anyMatch(r -> r.getMessage().contains(MemberValidator.GEB_UNVOLL)));
		
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
		List<ValidationResult> vr = mv.validate(m, "", leaders);
		assertTrue(MemberValidator.ZU_ALT, vr.stream().anyMatch(r -> r.getMessage().contains(MemberValidator.ZU_ALT)));
		
		m.setGebJahr(currentYear-st.getMax()-2);
		vr = mv.validate(m, "", leaders);
		assertTrue(MemberValidator.ZU_ALT, vr.stream().anyMatch(r -> r.getMessage().contains(MemberValidator.ZU_ALT)));
		
		m.setGebJahr(currentYear-st.getMin()+2);
		vr = mv.validate(m, "", leaders);
		assertTrue(MemberValidator.ZU_JUNG, vr.stream().anyMatch(r -> r.getMessage().contains(MemberValidator.ZU_JUNG)));

		// Just OK

		m.setGebJahr(currentYear-st.getMax()-1);
		vr = mv.validate(m, "", leaders);
		assertFalse(MemberValidator.ZU_ALT, vr.stream().anyMatch(r -> r.getMessage().contains(MemberValidator.ZU_ALT)));
		
		m.setGebJahr(currentYear-st.getMin()+1);
		vr = mv.validate(m, "", leaders);
		assertFalse(MemberValidator.ZU_JUNG, vr.stream().anyMatch(r -> r.getMessage().contains(MemberValidator.ZU_JUNG)));
	}

	@Test
	public void testValidationTruppVerein() throws Exception {
		
		Member m = new Member();
		m.setAktiv(true);
		
		List<ValidationResult> vr = mv.validate(m, "", leaders);
		assertTrue(MemberValidator.KEIN_TRUPP_FUNKTION, vr.stream().anyMatch(r -> r.getMessage().contains(MemberValidator.KEIN_TRUPP_FUNKTION)));

		m.setAktiv(false);
		
		vr = mv.validate(m, "", leaders);
		assertFalse(MemberValidator.KEIN_TRUPP_FUNKTION, vr.stream().anyMatch(r -> r.getMessage().contains(MemberValidator.KEIN_TRUPP_FUNKTION)));

		Squad trupp = new Squad();
		trupp.setType(SquadType.GUSP);
		m.setTrupp(trupp);

		vr = mv.validate(m, "", leaders);
		assertTrue(MemberValidator.INAKTIV_IM_TRUPP, vr.stream().anyMatch(r -> r.getMessage().contains(MemberValidator.INAKTIV_IM_TRUPP)));

	}
}
