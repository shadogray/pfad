/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateful;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;

import at.tfr.pfad.model.Activity_;
import at.tfr.pfad.model.Booking_;
import at.tfr.pfad.model.Member_;
import at.tfr.pfad.model.Payment;
import at.tfr.pfad.model.Payment_;
import at.tfr.pfad.model.Squad_;

@Stateful
public class PaymentDataModel extends DataModel<Payment, PaymentUI> {

	public PaymentDataModel() {
		uiClass = PaymentUI.class;
		entityClass = Payment.class;
	}

	public PaymentDataModel(Class<PaymentUI> uiClass, Class<Payment> entityClass) {
		super(uiClass, entityClass);
	}

	@Override
	public List<PaymentUI> convertToUiBean(List<Payment> list) {
		return list.stream().map(b->new PaymentUI(b)).collect(Collectors.toList());
	}
	
	@Override
	protected Predicate createFilterCriteriaForField(final String propertyName, final Object filterValue) {
		if (!(filterValue instanceof String) || StringUtils.isBlank((String)filterValue)) {
			return null;
		}
		switch(propertyName) {
		case "payer":
			return cb.or(cb.like(cb.lower(root.join(Payment_.payer).get(Member_.name)), "%"+filterValue+"%".toLowerCase()),
					cb.like(cb.lower(root.join(Payment_.payer).get(Member_.vorname)), "%"+filterValue+"%".toLowerCase()));
		
		case "member":
			return cb.or(cb.like(cb.lower(root.join(Payment_.bookings).get(Booking_.member).get(Member_.name)), "%"+filterValue+"%".toLowerCase()),
					cb.like(cb.lower(root.join(Payment_.bookings).get(Booking_.member).get(Member_.vorname)), "%"+filterValue+"%".toLowerCase()));
		
		case "squad":
			return cb.like(cb.lower(root.join(Payment_.bookings).get(Booking_.member).get(Member_.trupp).get(Squad_.name)), "%"+filterValue+"%".toLowerCase());

		case "activity":
			return cb.like(cb.lower(root.join(Payment_.bookings).get(Booking_.activity).get(Activity_.name)), "%"+filterValue+"%".toLowerCase());
		}
		return super.createFilterCriteriaForField(propertyName, filterValue);
	}

	@Override
	protected Path getPathForOrder(String propertyName) {
		switch(propertyName) {
		case "payer":
			return root.join(Payment_.payer).get(Member_.name);
		
		case "member":
			return root.join(Payment_.bookings).get(Booking_.member).get(Member_.name);
		
		case "squad":
			return root.join(Payment_.bookings).get(Booking_.member).get(Member_.trupp).get(Squad_.name);

		case "activity":
			return root.join(Payment_.bookings).get(Booking_.activity).get(Activity_.name);
		}
		return super.getPathForOrder(propertyName);
	}
}
