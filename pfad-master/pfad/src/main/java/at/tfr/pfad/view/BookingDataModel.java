/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.FetchType;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;

import at.tfr.pfad.BookingStatus;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Activity_;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Booking_;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Member_;
import at.tfr.pfad.model.Payment;
import at.tfr.pfad.model.Payment_;
import at.tfr.pfad.model.Squad;
import at.tfr.pfad.model.Squad_;

@RequestScoped
@Stateful
public class BookingDataModel extends DataModel<Booking, BookingUI> {

	@Inject
	private SquadRepository squadRepo;
	
	public BookingDataModel() {
		uiClass = BookingUI.class;
		entityClass = Booking.class;
	}
	
	@Override
	protected Root<Booking> getRoot() {
		Root<Booking> root = super.getRoot();
		return root;
	}

	@Override
	protected Root<Booking> getCountRoot() {
		Root<Booking> root = super.getCountRoot();
		return root;
	}
	
	@Override
	protected Path<Booking>[] getGroupByRoots() {
		return new Path[] { root };
	}
	
	public BookingDataModel(Class<BookingUI> uiClass, Class<Booking> entityClass) {
		super(uiClass, entityClass);
	}

	@Override
	protected CriteriaQuery<Booking> createCriteria(boolean addOrder) {
		CriteriaQuery<Booking> crit = super.createCriteria(addOrder);
		//root.fetch(Booking_.member); //.fetch(Member_.funktionen, JoinType.LEFT);
		//root.fetch(Booking_.activity);
		//root.fetch(Booking_.payments, JoinType.LEFT);
		return crit;
	}
	
	@Override
	protected CriteriaQuery<Long> createCountCriteriaQuery() {
		CriteriaQuery<Long> crit = super.createCountCriteriaQuery();
		//root.join(Booking_.member);
		return crit;
	}
	
	@Override
	public List<BookingUI> convertToUiBean(List<Booking> list) {
		if (!list.isEmpty()) {
			CriteriaQuery<Tuple> tcq = cb.createTupleQuery();
			Root<Booking> tr = tcq.from(Booking.class);
			tr.fetch(Booking_.payments, JoinType.LEFT);
			Subquery<Squad> squadSubLead = tcq.subquery(Squad.class);
			Subquery<Squad> squadSubAss = tcq.subquery(Squad.class);
			Root<Squad> squadLead = squadSubLead.from(Squad.class);
			Root<Squad> squadAss = squadSubAss.from(Squad.class);
			Path<Member> memb = tr.get(Booking_.member);
			Join<Member,Squad> trupp = tr.join(Booking_.member).join(Member_.trupp, JoinType.LEFT);

			tcq.multiselect(tr, 
					tr.get(Booking_.activity), 
					memb, 
					trupp, 
					cb.exists(squadSubLead.select(squadLead).where(cb.or(cb.equal(memb, squadLead.get(Squad_.leaderFemale)), cb.equal(memb,  squadLead.get(Squad_.leaderMale))))), 
					cb.exists(squadSubAss.select(squadAss).where(cb.isMember(memb, squadAss.get(Squad_.assistants)))))
			.where(tr.in(list))
			.orderBy(createOrders(tr))
			.distinct(true);
			
			List<Tuple> res = entityManager.createQuery(tcq).getResultList();
			return res.stream().map(t -> new BookingUI(
					t.get(0, Booking.class), 
					t.get(1, Activity.class), 
					t.get(2, Member.class), 
					t.get(3,Squad.class), 
					t.get(0, Booking.class).getPayments(), 
					t.get(4, Boolean.class), 
					t.get(5, Boolean.class)))
					.collect(Collectors.toList());
		} 
		return Collections.emptyList();
	}
	
	@Override
	protected Predicate createFilterCriteriaForField(final String propertyName, final Object filterValue, CriteriaQuery<?> criteriaQuery) {
		if (!(filterValue instanceof String) || StringUtils.isBlank((String)filterValue)) {
			return null;
		}
		String val = filterValue.toString().toLowerCase();
		switch(propertyName) {
		case "member":
			return getSplittedPredicateName(root.join(Booking_.member), val);
		case "strasse":
			return cb.like(cb.lower(root.join(Booking_.member).get(Member_.strasse)), "%"+val+"%");
		case "ort":
			return cb.like(cb.lower(root.join(Booking_.member).get(Member_.ort)), "%"+val+"%");
		case "activity":
			return cb.like(cb.lower(root.join(Booking_.activity).get(Activity_.name)), "%"+val+"%");
		case "squadName":
			return cb.like(cb.lower(root.join(Booking_.member).join(Member_.trupp).get(Squad_.name)), "%"+val+"%");
		case "status":
			BookingStatus bookingStatus = getBookingStatus(val);
			if (bookingStatus != null) {
				return cb.equal(root.get(Booking_.status), bookingStatus);
			}
			return null;
		case "payed":
			boolean finished = Boolean.parseBoolean((String)val);
			if (finished) {
				return cb.equal(root.join(Booking_.payments).get(Payment_.finished), finished);
			} else {
				Subquery<Payment> sq = criteriaQuery.subquery(Payment.class);
				Root<Payment> sr = sq.from(Payment.class);
				List<Predicate> paymentStatOr = new ArrayList<>();
				paymentStatOr.add(cb.and(cb.isMember(root, sr.get(Payment_.bookings)), cb.equal(sr.get(Payment_.finished), true)));
				if ("false".equalsIgnoreCase(val)) {
					paymentStatOr.add(cb.and(cb.isMember(root, sr.get(Payment_.bookings)), cb.equal(sr.get(Payment_.aconto), true)));
				}
				sq.select(sr).where(cb.or(paymentStatOr.toArray(new Predicate[paymentStatOr.size()])));
				Predicate notFin = cb.not(cb.exists(sq));
				if ("anz".equalsIgnoreCase(val)) {
					notFin = cb.and(notFin, cb.equal(root.join(Booking_.payments).get(Payment_.aconto), true));
				}
				return notFin;
			}
		}
		return super.createFilterCriteriaForField(propertyName, filterValue, criteriaQuery);
	}

	private BookingStatus getBookingStatus(final String val) {
		for (BookingStatus bs : BookingStatus.values()) {
			if (bs.name().toLowerCase().contains(val.toLowerCase())) {
				return bs;
			}
		}
		return null;
	}
	
	@Override
	protected Path getPathForOrder(Root<Booking> path, String propertyName) {
		switch(propertyName) {
		case "member":
			return path.get(Booking_.member).get(Member_.name);
		case "strasse":
			return path.get(Booking_.member).get(Member_.strasse);
		case "ort":
			return path.get(Booking_.member).get(Member_.ort);
		case "activity":
			return path.get(Booking_.activity).get(Activity_.name);
		case "squadName":
			return path.get(Booking_.member).get(Member_.trupp).get(Squad_.name);
		case "payed":
			return path.join(Booking_.payments).get(Payment_.finished);
		}
		return super.getPathForOrder(path, propertyName);
	}
}
