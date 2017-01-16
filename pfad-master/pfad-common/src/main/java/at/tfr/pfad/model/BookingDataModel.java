/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.ejb.Stateful;
import javax.enterprise.inject.Default;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;

import at.tfr.pfad.BookingStatus;

@Stateful
public class BookingDataModel extends DataModel<Booking, BookingUI> {

	public BookingDataModel() {
		uiClass = BookingUI.class;
		entityClass = Booking.class;
	}

	public BookingDataModel(Class<BookingUI> uiClass, Class<Booking> entityClass) {
		super(uiClass, entityClass);
	}

	@Override
	public BookingUI convert(Booking entity) {
		return new BookingUI(entity);
	}
	
	@Override
    protected CriteriaQuery<Booking> createCriteria(boolean addOrder) {
		CriteriaQuery<Booking> query = super.createCriteria(addOrder);
        Fetch<Booking,Member> member = root.fetch(Booking_.member, JoinType.LEFT);
        Fetch<Booking,Activity> activity = root.fetch(Booking_.activity, JoinType.LEFT);
        Fetch<Booking,Squad> squad = root.fetch(Booking_.squad, JoinType.LEFT);
        return query;
    }
	
	@Override
	protected CriteriaQuery<Booking> groupBy(CriteriaQuery<Booking> crit) {
		return crit;
	}
	
	@Override
	protected Predicate createFilterCriteriaForField(final String propertyName, final Object filterValue, CriteriaQuery<?> criteriaQuery) {
		if (!(filterValue instanceof String) || StringUtils.isBlank((String)filterValue)) {
			return null;
		}
		String val = filterValue.toString().toLowerCase();
		List<Predicate> list = new ArrayList<>();
		switch(propertyName) {
		case "member":
			Stream.of(val.split(" +")).forEach(v -> { 
				list.add(cb.or(
						cb.like(cb.lower(root.join(Booking_.member).get(Member_.name)), "%"+v+"%"),
						cb.like(cb.lower(root.join(Booking_.member).get(Member_.vorname)), "%"+v+"%")
						));
			});
			return cb.and(list.toArray(new Predicate[]{}));
		case "strasse":
			Stream.of(val.split(" +")).forEach(v -> { 
				list.add(cb.or(
					cb.like(cb.lower(root.join(Booking_.member).get(Member_.strasse)), "%"+v+"%"),
					cb.like(cb.lower(root.join(Booking_.member).get(Member_.ort)), "%"+v+"%"),
					cb.like(cb.lower(root.join(Booking_.member).get(Member_.plz)), "%"+v+"%")
					));
			});
			return cb.and(list.toArray(new Predicate[]{}));
		case "activity":
			return cb.like(cb.lower(root.join(Booking_.activity).get(Activity_.name)), "%"+val+"%");
		case "squadName":
			return cb.like(cb.lower(root.join(Booking_.member).get(Member_.trupp).get(Squad_.name)), "%"+val+"%");
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
				sq.select(sr).where(cb.isMember(root, sr.get(Payment_.bookings)), cb.equal(sr.get(Payment_.finished), !finished));
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
	protected Path getPathForOrder(String propertyName) {
		switch(propertyName) {
		case "member":
			return root.join(Booking_.member).get(Member_.name);
		case "activity":
			return root.join(Booking_.activity).get(Activity_.name);
		case "squadName":
			return root.join(Booking_.member).get(Member_.trupp).get(Squad_.name);
		case "payed":
			return root.join(Booking_.payments).get(Payment_.finished);
		}
		return super.getPathForOrder(propertyName);
	}
}
