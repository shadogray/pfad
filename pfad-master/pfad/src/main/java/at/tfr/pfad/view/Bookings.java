package at.tfr.pfad.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import at.tfr.pfad.model.Activity_;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Booking_;
import at.tfr.pfad.model.Member_;

@Named
@ApplicationScoped
public class Bookings implements Serializable {

	private Logger log = Logger.getLogger(getClass());

	@Inject
	private transient EntityManager entityManager;

	public List<Booking> filtered(final String filter) {
		log.debug("filter: " + filter);
		CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Booking> cq = cb.createQuery(Booking.class);
		Root<Booking> root = cq.from(Booking.class);
		CriteriaQuery<Booking> query = cq.select(root);
		List<Predicate> preds = new ArrayList<>();
		if (StringUtils.isNotBlank(filter) && filter.length() > 2) {
			Stream.of(filter.toLowerCase().split(" ")).forEach(v->preds.add(cb.or(predicatesFor(v, cb, root))));
			cq.where(cb.and(preds.toArray(new Predicate[preds.size()])));
		}
		return this.entityManager.createQuery(query.distinct(true)).setMaxResults(10).getResultList();
	}
	
	Predicate[] predicatesFor(String value, CriteriaBuilder cb, Root<Booking> root) {
		List<Predicate> list = new ArrayList<>();
		list.add(cb.like(cb.lower(root.get(Booking_.member).get(Member_.name)), "%"+value+"%"));
		list.add(cb.like(cb.lower(root.get(Booking_.member).get(Member_.vorname)), "%"+value+"%"));
		list.add(cb.like(cb.lower(root.get(Booking_.activity).get(Activity_.name)), "%"+value+"%"));
		return list.toArray(new Predicate[list.size()]);
	}

	@SuppressWarnings("unchecked")
	public List<Long> allIds() {
		return (List<Long>)entityManager.createQuery("select b.id from Booking b").getResultList();
	}
}
