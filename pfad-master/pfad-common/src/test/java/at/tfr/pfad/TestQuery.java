package at.tfr.pfad;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.hibernate.jpa.QueryHints;
import org.junit.Test;
import org.junit.runner.RunWith;

import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.BookingUI;
import at.tfr.pfad.model.Booking_;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Member_;
import at.tfr.pfad.model.Payment_;
import at.tfr.pfad.model.Squad;

@RunWith(CdiTestRunner.class)
public class TestQuery {

	@Inject
	private EntityManager em;
	
	@Test
	public void testQueryBookingUI() throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		
		CriteriaQuery<BookingUI> criteria = builder.createQuery(BookingUI.class);
		Root<Booking> root = criteria.from(Booking.class);
		Join<Booking,Member> member = root.join(Booking_.member, JoinType.LEFT);
		Join<Booking,Activity> activity = root.join(Booking_.activity, JoinType.LEFT);
		Join<Booking,Squad> squad = root.join(Booking_.squad, JoinType.LEFT);

		criteria.multiselect(root);
		TypedQuery<BookingUI> query = em.createQuery(criteria.distinct(true));
		
		//query.setHint(QueryHints.HINT_LOADGRAPH, em.getEntityGraph(Booking.Booking));
		query.setFirstResult(0).setMaxResults(10);
		
		query.getResultList();
		
	}
	
	@Test
	public void testQueryBooking() throws Exception {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		CriteriaQuery<Booking> criteria = cb.createQuery(Booking.class);
		Root<Booking> root = criteria.from(Booking.class);
		Fetch<Booking,Member> member = root.fetch(Booking_.member, JoinType.LEFT);
		Fetch<Booking,Activity> activity = root.fetch(Booking_.activity, JoinType.LEFT);
		
		criteria.where(cb.or(cb.like(cb.lower(root.join(Booking_.member).get(Member_.name)), "%test%")));
		
		TypedQuery<Booking> query = em.createQuery(criteria.distinct(true));
		query.setHint(QueryHints.HINT_LOADGRAPH, em.getEntityGraph(Booking.Booking));
		query.setFirstResult(0).setMaxResults(10);
		
		query.getResultList();
		
	}
	
}
