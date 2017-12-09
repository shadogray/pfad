package at.tfr.pfad;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.util.QueryExecutor;
import at.tfr.pfad.util.TemplateUtils;

@RunWith(CdiTestRunner.class)
public class TestMailTools {

	@Inject
	private QueryExecutor qe;
	@Inject
	private MemberRepository memberRepo;
	@Inject
	private TemplateUtils tu;
	
	@Test
	public void testExecutor() throws Exception {
		List<Map<String, Object>> list = qe.execute("select m.name as Name, m.vorname as Vorname, m.email as Email from Member m", false);
		Assert.assertFalse(list.isEmpty());
	}
	
	@Test
	public void testMailTemplate() throws Exception {
		
		String template = "Das ist eine Nachricht an ${Email}.";
		
		List<Map<String, Object>> list = qe.execute("select m.name as Name, m.vorname as Vorname, m.email as Email "
				+ " from Member m where m.id = 1", false);

		Map<String,Object> map = list.get(0);
		String res = tu.replace(template, map);
		Assert.assertEquals("Replacement failed.", "Das ist eine Nachricht an email.", res);
	}
	
	@Test
	public void testMailTemplatePropNav() throws Exception {
		
		String template = "Das ist eine Nachricht an ${Member.email}.";
		
		List<Map<String, Object>> list = qe.execute("select m as Member, m.name as Name, m.vorname as Vorname, m.email as Email "
				+ " from Member m where m.id = 1", false);

		Map<String,Object> map = list.get(0);
		String res = tu.replace(template, map);
		Assert.assertEquals("Replacement failed.", "Das ist eine Nachricht an email.", res);
	}
	
	@Before
	public void init() {

		Member m = memberRepo.findBy(1L);
		if (m == null) {
			m = new Member();
			m.setId(1L);
			memberRepo.persist(m);
		}
		m.setName("name");
		m.setVorname("vorname");
		m.setEmail("email");
		memberRepo.save(m);
	}

}
