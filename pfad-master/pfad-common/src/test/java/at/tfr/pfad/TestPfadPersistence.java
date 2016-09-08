package at.tfr.pfad;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Payment;
import at.tfr.pfad.model.Squad;

@RunWith(CdiTestRunner.class)
public class TestPfadPersistence {

	@Inject
	private EntityManager em;
	@Inject
	private EntityManagerFactory emf;
	@Inject
	private MemberRepository memberRepo;

	@Before
	public void init() {
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testObjectGraph() throws Exception {
		EntityTransaction tx = em.getTransaction();
		tx.begin();

		Member m = new Member();
		m.setName("Name");
		m.setVorname("Vorname");
		em.persist(m); // assure ID not null for equals()

		Member sibling = new Member();
		sibling.setName("Sibling");
		em.persist(sibling); // assure ID not null for equals()
		
		Member parent = new Member();
		parent.setName("Parent");
		parent.getSiblings().add(sibling);
		parent.getSiblings().add(m);
		em.persist(parent); // assure ID not null for equals()
		
		Squad s = new Squad();
		s.setType(SquadType.RARO);
		s.setName("Trupp");
		m.setTrupp(s);

		Payment p = new Payment();
		p.setPayer(m);
		p.setType(PaymentType.Donation);
		p.setAmount(99.99F);
		p.setAconto(false);
		p.setFinished(false);
		p.setComment("Comment");

		Booking b = new Booking();
		b.setMember(m);
		b.setStatus(BookingStatus.created);
		//b.getPayments().add(p);
		p.getBookings().add(b);

		em.persist(s);
		em.persist(b);
		em.persist(p);
		tx.commit();
		
		assertNotNull("persisting parent association failed", parent.getId());
		assertNotNull("persisting sibling association failed", sibling.getId());
		em.close();

		em = emf.createEntityManager();
		Member po = em.find(Member.class, parent.getId());
		assertTrue("sibling association empty", !po.getSiblings().isEmpty());
		Member mo = em.find(Member.class, m.getId());
		assertTrue("parent association empty", !mo.getParents().isEmpty());
		em.close();
		
		mo = memberRepo.fetchBy(m.getId());
		assertTrue("parent association empty", !mo.getParents().isEmpty());
	}
	
	@Test
	public void testPersistence() throws Exception {

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		Member m = new Member();
		Payment p = new Payment();
		Booking orig = new Booking();
		orig.setMember(m);
		orig.setStatus(BookingStatus.created);
		orig.getPayments().add(p);
		em.persist(m);
		em.persist(orig);
		em.getTransaction().commit();
		em.close();

		em = emf.createEntityManager();
		Booking b = em.find(Booking.class, orig.getId());
		Set<Payment> payments = b.getPayments();
		payments.size();
		em.close();
		payments.size();

		Set<Payment> payments2 = b.getPayments();
		payments2.size();
	}

	@Ignore
	@Test
	public void testPersistenceMerge() throws Exception {

		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();

		Member m = new Member();
		m.setName("Name");
		m.setVorname("Vorname");
		
		Squad s = new Squad();
		s.setType(SquadType.RARO);
		s.setName("Trupp");
		m.setTrupp(s);

		Payment p = new Payment();
		p.setPayer(m);
		p.setType(PaymentType.Donation);
		p.setAmount(99.99F);
		p.setAconto(false);
		p.setFinished(false);
		p.setComment("Comment");

		Booking b = new Booking();
		b.setMember(m);
		b.setStatus(BookingStatus.created);
		//b.getPayments().add(p);
		p.getBookings().add(b);

		em.persist(m);
		em.persist(s);
		em.persist(b);
		em.persist(p);
		tx.commit();

		assertNotNull("keine Member.ID gesetzt", m.getId());
		assertNotNull("keine Booking.ID gesetzt", b.getId());
		assertNotNull("keine Payment.ID gesetzt", p.getId());

		Member mo = new Member();
		mo.setId(m.getId());
		mo.setName(m.getName());
		mo.setVersion(m.getVersion());
		mo.setTrupp(s);

		em.clear();
		tx.begin();
		em.merge(mo);
		tx.commit();

		em.clear();
		Booking bo = em.find(Booking.class, b.getId());
		assertTrue("Booking.Payments ist leer", !bo.getPayments().isEmpty());
		assertNotNull("Payer nicht gesetzt", bo.getPayments().iterator().next().getPayer());

		em.clear();
		Member mx = em.find(Member.class, m.getId());
		assertNotNull("Member hat keinen Trupp", mx.getTrupp());
		
		em.clear();
		Payment px = new Payment();
		px.setId(p.getId());
		px.setVersion(p.getVersion());
		px.setPayer(m);
		px.setType(p.getType());
		px.setAmount(p.getAmount());
		px.setAconto(p.getAconto());
		px.setFinished(p.getFinished());
		px.setComment("Comment2");
		tx.begin();
		em.merge(px);
		/* see log:
		 * Hibernate: 
    delete 
    from
        Payment_Booking 
    where
        payments_id=?
		 */
		tx.commit();
		
		em.clear();
		px = em.find(Payment.class, p.getId());
		// will not work :-(
		assertTrue("Payment.bookings Assoziation verloren", !px.getBookings().isEmpty());
		assertEquals("Payment.bookings Assoziation korrupt", b, px.getBookings().iterator().next());
		
	}
}
