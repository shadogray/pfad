package at.tfr.pfad;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.deltaspike.data.api.EntityGraph;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.dao.PaymentRepository;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Payment;

@RunWith(CdiTestRunner.class)
public class TestPfadRepositories {

	@Inject
	private MemberRepository memberRepo;
	@Inject
	private PaymentRepository paymentRepo;
	@Inject
	private EntityManager em;
	
	@Test
	public void testMemberRepository() throws Exception {
		
		Member c = new Member();
		c.setName("Child");
		c = memberRepo.save(c);
		Member m = new Member();
		m.setName("Name");
		m.getSiblings().add(c);
		Member saved = memberRepo.save(m);
		
		assertTrue("save failed", saved.getId() != null);
		assertTrue("entity not found", em.find(Member.class, saved.getId()) != null);

		assertTrue("created not set", saved.getCreated() != null);
		
		List<Member> test = memberRepo.fetchAll(1);
		assertFalse("fetch failed", test.isEmpty());
		
		assertFalse("EntityGraph failed", saved.getSiblingIds().isEmpty());
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testPaymentRepo() throws Exception {
		
		paymentRepo.findByBookingIdOrderByIdDesc(1L);
		
		paymentRepo.findById(1L);
		
		paymentRepo.findByIdsOrderByIdDesc(LongStream.of(1,2,3).boxed().collect(Collectors.toList()));
		
		paymentRepo.findByPayerIdOrderByIdDesc(1L);
		
		List<Payment> ps = paymentRepo.findByBookingIdOrderByIdDesc(1L);
	}
	
}
