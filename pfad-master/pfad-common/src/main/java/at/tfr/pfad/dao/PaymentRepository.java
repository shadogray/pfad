package at.tfr.pfad.dao;

import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.deltaspike.data.api.EntityGraph;
import org.apache.deltaspike.data.api.EntityManagerDelegate;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.SingleResultType;

import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Payment;

@ApplicationScoped
@Repository
public abstract class PaymentRepository implements EntityRepository<Payment, Long>, EntityManagerDelegate<Payment> {

	@Inject
	private EntityManager em;
	
	@Query(singleResult=SingleResultType.OPTIONAL)
	@EntityGraph(paths = {"payer", "bookings"})
	public abstract Payment findById(Long id);
	
	@EntityGraph(paths = {"payer", "bookings"})
	@Query("select p from Payment p where p.id in ?1 order by p.id desc")
	public abstract List<Payment> findByIdsOrderByIdDesc(List<Long> ids);
	
	@EntityGraph(paths = {"payer", "bookings"})
	@Query("select p from Payment p where p.payer.id = ?1 order by p.id desc")
	public abstract List<Payment> findByPayerIdOrderByIdDesc(Long id);
	
	@EntityGraph(paths = {"payer"})
	@Query("select p from Payment p, Booking b where b member of p.bookings and b.id = ?1 order by p.id desc")
	public abstract List<Payment> findByBookingIdOrderByIdDesc(Long id);
	
	@SuppressWarnings("unchecked")
	public List<Payment> findByIds(Collection<Long> ids) {
		return em.createQuery("select p from Payment p where p.id in :ids")
				.setParameter("ids", ids).getResultList();
	}

	@Query(named = "PaymentsForBooking")
	public abstract List<Payment> findByBooking(Booking b);

}
