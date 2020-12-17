package at.tfr.pfad;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.inject.Inject;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityTransaction;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
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
	
	private Member member;
	
	@Test
	public void testExecutor() throws Exception {
		List<List<Entry<String, Object>>> list = qe.execute("select m.name as Name, m.vorname as Vorname, m.email as Email from Member m", false);
		Assert.assertFalse(list.isEmpty());
	}
	
	@Test
	public void testExecutorNoNames() throws Exception {
		List<List<Entry<String, Object>>> list = qe.execute("select m.name, m.vorname, m.email from Member m", false);
		Assert.assertFalse(list.isEmpty());
	}
	
	@Test
	public void testMailTemplate() throws Exception {
		
		String template = "Das ist eine Nachricht an ${Email}.";
		
		List<List<Entry<String, Object>>> list = qe.execute("select m.name as Name, m.vorname as Vorname, m.email as Email "
				+ " from Member m where m.id = "+member.getId(), false);

		List<Entry<String,Object>> map = list.get(0);
		String res = tu.replace(template, map);
		Assert.assertEquals("Replacement failed.", "Das ist eine Nachricht an email.", res);
	}
	
	@Test
	public void testMailTemplatePropNav() throws Exception {
		
		String template = "Das ist eine Nachricht an ${Member.email}.";
		
		List<List<Entry<String, Object>>> list = qe.execute("select m as Member, m.name as Name, m.vorname as Vorname, m.email as Email "
				+ " from Member m where m.id = "+member.getId(), false);

		List<Entry<String,Object>> map = list.get(0);
		String res = tu.replace(template, map);
		Assert.assertEquals("Replacement failed.", "Das ist eine Nachricht an email.", res);
	}
	
	@Test(expected = AddressException.class)
	public void testMailAddresses() throws Exception {
		String addr = "test@test@test.at;";
		MimeMessage mail = new MimeMessage(Session.getDefaultInstance(new Properties()));
		mail.setRecipients(RecipientType.TO, InternetAddress.parse(addr));
	}
	
	@Test
	public void testStringSubstitutor() throws Exception {
		String template = "Das ist ein ${test:+PositiveContent}";
		Map<String,Object> map = new HashMap<>();
		map.put("test", "REPLACEMENT");
		String result = tu.replace(template, map);
		Assert.assertEquals("positive replacement failed: " + result, "Das ist ein PositiveContent", result);
	}
	
	@Test
	public void testStringSubstitutorRecursive() throws Exception {
		String template = "Das ist ein ${test:+${bla:-PositiveContent}}";
		Map<String,Object> map = new HashMap<>();
		map.put("test", "REPLACEMENT");
		String result = tu.replace(template, map);
		Assert.assertEquals("positive replacement failed: " + result, "Das ist ein PositiveContent", result);
	}
	
	@Test
	public void testStringSubstitutorRecursiveReverse() throws Exception {
		String template = "Das ist ein ${bla:-${test:+PositiveContent}}";
		Map<String,Object> map = new HashMap<>();
		map.put("test", "REPLACEMENT");
		String result = tu.replace(template, map);
		Assert.assertEquals("positive replacement failed: " + result, "Das ist ein PositiveContent", result);
	}
	
	@Test
	public void testStringSubstitutorNormal() throws Exception {
		String template = "Das ist ein ${test:-PositiveContent}";
		Map<String,Object> map = new HashMap<>();
		map.put("test", "REPLACEMENT");
		String result = tu.replace(template, map);
		Assert.assertEquals("positive replacement failed: " + result, "Das ist ein REPLACEMENT", result);
	}
	
	@Test
	public void testStringSubstitutorNormalRecursive() throws Exception {
		String template = "Das ist ein ${${test:-PositiveContent}}";
		Map<String,Object> map = new HashMap<>();
		map.put("repl", "REPLACEMENT");
		map.put("test", "repl");
		String result = tu.replace(template, map);
		Assert.assertEquals("positive replacement failed: " + result, "Das ist ein REPLACEMENT", result);
	}
	
	@Test
	public void testStringSubstitutorNormalRecursiveDefault() throws Exception {
		String template = "Das ist ein ${${${bla:-test}}}";
		Map<String,Object> map = new HashMap<>();
		map.put("repl", "REPLACEMENT");
		map.put("test", "repl");
		String result = tu.replace(template, map);
		Assert.assertEquals("positive replacement failed: " + result, "Das ist ein REPLACEMENT", result);
	}
	
	@Test
	public void testScriptedStringSubstitutor() throws Exception {
		Map<String,Object> map = new HashMap<>();
		map.put("repl", "REPLACEMENT");
		map.put("test", "repl");
		map.put("checked", true);
		map.put("unchecked", false);
		String template = "Das ist ein ${js:-if (checked) repl; else test;}";
		String result = tu.replace(template, map);
		Assert.assertEquals("positive replacement failed: " + result, "Das ist ein REPLACEMENT", result);
		
		template = "Das ist ein ${js:-if (unchecked) repl; else test;}";
		result = tu.replace(template, map);
		Assert.assertEquals("positive replacement failed: " + result, "Das ist ein repl", result);

		template = "Das ist ein ${${js:-if (unchecked) repl; else test;}}";
		result = tu.replace(template, map);
		Assert.assertEquals("positive replacement failed: " + result, "Das ist ein REPLACEMENT", result);
	}

	EntityTransaction tx;
	@Before
	public void init() {

		tx = memberRepo.getTransaction();
		member = memberRepo.findBy(1L);
		if (member == null) {
			member = new Member();
			//member.setId(1L); pretty useless.. 
			member = memberRepo.merge(member);
		}
		member.setName("name");
		member.setVorname("vorname");
		member.setEmail("email");
		memberRepo.saveAndFlush(member);
		tx.commit();
		tx = memberRepo.getTransaction();
	}
	
	@After
	public void tearDown() {
		if (tx != null && tx.isActive())
			tx.rollback();
	}

	
}
