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
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Payment;

@ApplicationScoped
@Repository
public abstract class BookingRepository implements EntityRepository<Booking, Long>, CriteriaSupport<Booking>, EntityManagerDelegate<Booking> {

	@Inject
	private EntityManager em;
	
	@Query(singleResult=SingleResultType.OPTIONAL)
	@EntityGraph(paths = {"member", "activity", "squad", "payments"})
	public abstract Booking findById(Long id);
	
	@Query
	public abstract List<Booking> findByActivity(Activity activity);
	
	@Query(named = "BookingsForPayment")
	@EntityGraph(paths = {"member", "activity", "squad"})
	public abstract List<Booking> findByPayment(Payment payment);
	
	@Query(named = "BookingsForPaymentIds")
	public abstract List<Booking> findByPaymentIds(List<Long> paymentIds);
	
	@SuppressWarnings("unchecked")
	public List<Booking> findByIds(Collection<Long> ids) {
		return em.createQuery("select b from Booking b where b.id in :ids")
				.setParameter("ids", ids).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Booking> findByMemberNames(String text, Activity activity) {
		List<Booking> list = em.createQuery("select b from Booking b "
				+ "where b.member.trupp != null and b.activity = :activity "
				+ "and locate(b.member.name, :text) > 0 and locate(b.member.vorname, :text) > 0")
		.setParameter("text", text)
		.setParameter("activity", activity)
		.getResultList();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<Booking> findByMemberNameOnly(String text, Activity activity) {
		List<Booking> list = em.createQuery("select b from Booking b "
				+ "where b.member.trupp != null and b.activity = :activity "
				+ "and locate(b.member.name, :text) > 0")
					.setParameter("text", text)
					.setParameter("activity", activity)
					.getResultList();
		return list;
	}
	
}
