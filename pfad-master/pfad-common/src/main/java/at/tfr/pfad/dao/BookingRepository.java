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
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Payment;

@ApplicationScoped
@Repository
public abstract class BookingRepository implements EntityRepository<Booking, Long>, CriteriaSupport<Booking>, EntityManagerDelegate<Booking> {

	@Inject
	private EntityManager em;
	
	@Query
	@EntityGraph(paths = {"member", "activity", "squad", "payments"})
	public abstract Booking findById(Long id);
	
	@Query
	public abstract List<Booking> findByActivity(Activity activity);
	
	@Query(named = "BookingsForPayment")
	public abstract List<Booking> findByPayment(Payment payment);
	
	@SuppressWarnings("unchecked")
	public List<Booking> findByIds(Collection<Long> ids) {
		return em.createQuery("select b from Booking b where b.id in :ids")
				.setParameter("ids", ids).getResultList();
	}
}
