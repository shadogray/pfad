package at.tfr.pfad.dao;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;

import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Booking;

@Repository
public abstract class BookingRepository implements EntityRepository<Booking, Long>{

	@Inject
	private EntityManager em;
	
	@Query
	public abstract List<Booking> findByActivity(Activity activity);
	
	@SuppressWarnings("unchecked")
	public List<Booking> findByIds(Collection<Long> ids) {
		return em.createQuery("select b from Booking b where b.id in :ids")
				.setParameter("ids", ids).getResultList();
	}
}
