/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;

import at.tfr.pfad.BookingStatus;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Activity_;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Booking_;
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

	public BookingDataModel(Class<BookingUI> uiClass, Class<Booking> entityClass) {
		super(uiClass, entityClass);
	}

	@Override
	public List<BookingUI> convertToUiBean(List<Booking> list) {
		final List<Squad> squads = squadRepo.findAll();
		return list.stream().map(b->new BookingUI(b, squads)).collect(Collectors.toList());
	}
	
	@Override
	protected Predicate createFilterCriteriaForField(final String propertyName, final Object filterValue, CriteriaQuery<?> criteriaQuery) {
		if (!(filterValue instanceof String) || StringUtils.isBlank((String)filterValue)) {
			return null;
		}
		String val = filterValue.toString().toLowerCase();
		switch(propertyName) {
		case "member":
			return cb.or(cb.like(cb.lower(root.join(Booking_.member).get(Member_.name)), "%"+val+"%"),
					cb.like(cb.lower(root.join(Booking_.member).get(Member_.vorname)), "%"+val+"%"));
		case "strasse":
			return cb.like(cb.lower(root.join(Booking_.member).get(Member_.strasse)), "%"+val+"%");
		case "ort":
			return cb.like(cb.lower(root.join(Booking_.member).get(Member_.ort)), "%"+val+"%");
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
