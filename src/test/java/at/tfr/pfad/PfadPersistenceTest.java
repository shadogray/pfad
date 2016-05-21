package at.tfr.pfad;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Payment;

@RunWith(CdiTestRunner.class)
public class PfadPersistenceTest {

	@PersistenceUnit(unitName="pfad")
	private EntityManagerFactory pfadPu;
	
	@Before
	public void init() {
		pfadPu = Persistence.createEntityManagerFactory("pfad");
	}
	
	@Test
	public void testPersistence() throws Exception {
		
		EntityManager em = pfadPu.createEntityManager();
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
		
		em = pfadPu.createEntityManager();
		Booking b = em.find(Booking.class, orig.getId());
		Set<Payment> payments = b.getPayments();
		payments.size();
		em.close();
		payments.size();
		
		Set<Payment> payments2 = b.getPayments();
		payments2.size();
	}
	
}
