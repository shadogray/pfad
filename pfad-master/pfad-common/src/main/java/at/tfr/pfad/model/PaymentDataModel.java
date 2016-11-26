/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;

@RequestScoped
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
	protected CriteriaQuery<Payment> createCriteria(boolean addOrder) {
		CriteriaQuery<Payment> query = super.createCriteria(addOrder);
		root.fetch(Payment_.payer, JoinType.LEFT);
		//Fetch<Payment, Booking> bookings = root.fetch(Payment_.bookings, JoinType.LEFT);
		//bookings.fetch(Booking_.activity, JoinType.LEFT);
		//Fetch<Booking, Member> member = bookings.fetch(Booking_.member, JoinType.LEFT);
		//member.fetch(Member_.trupp, JoinType.LEFT);
		//member.fetch(Member_.funktionen, JoinType.LEFT);
		//bookings.fetch(Booking_.squad, JoinType.LEFT);
		return query;
	}
	
	@Override
	public List<PaymentUI> convertToUiBean(List<Payment> list) {
		return list.stream().map(b->new PaymentUI(b)).collect(Collectors.toList());
	}
	
	@Override
	protected Predicate createFilterCriteriaForField(final String propertyName, final Object filterValue, CriteriaQuery<?> criteriaQuery) {
		if (!(filterValue instanceof String) || StringUtils.isBlank((String)filterValue)) {
			return null;
		}
		String val = filterValue.toString().toLowerCase();
		switch(propertyName) {
		case "payer":
			return cb.or(cb.like(cb.lower(root.join(Payment_.payer).get(Member_.name)), "%"+val+"%"),
					cb.like(cb.lower(root.join(Payment_.payer).get(Member_.vorname)), "%"+val+"%"));
		
		case "member":
			return cb.or(cb.like(cb.lower(root.join(Payment_.bookings).get(Booking_.member).get(Member_.name)), "%"+val+"%"),
					cb.like(cb.lower(root.join(Payment_.bookings).get(Booking_.member).get(Member_.vorname)), "%"+val+"%"));
		
		case "squad":
			return cb.like(cb.lower(root.join(Payment_.bookings).get(Booking_.member).get(Member_.trupp).get(Squad_.name)), "%"+val+"%");

		case "activity":
			return cb.like(cb.lower(root.join(Payment_.bookings).get(Booking_.activity).get(Activity_.name)), "%"+val+"%");
		}
		return super.createFilterCriteriaForField(propertyName, val, criteriaQuery);
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
