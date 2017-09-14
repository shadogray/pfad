package at.tfr.pfad.dao;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.deltaspike.data.api.EntityManagerDelegate;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;

import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Payment;

@Repository
public abstract class PaymentRepository implements EntityRepository<Payment, Long>, EntityManagerDelegate<Payment> {

	@Inject
	private EntityManager em;
	
	@SuppressWarnings("unchecked")
	public List<Payment> findByIds(Collection<Long> ids) {
		return em.createQuery("select p from Payment p where p.id in :ids")
				.setParameter("ids", ids).getResultList();
	}

	@Query(named = "PaymentsForBooking")
	public abstract List<Payment> findByBooking(Booking b);

}
