/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.enterprise.inject.Default;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.SortMeta;

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
	protected List<PaymentUI> getRowDataInternal(int first, int pageSize, List<SortMeta> sortMetas,
			Map<String, Object> filters) {
		List<PaymentUI> data = super.getRowDataInternal(first, pageSize, sortMetas, filters);
		bookingRepo.findByPaymentIds(data.stream().map(p->p.getId()).collect(Collectors.toList()));
		data.forEach(pui->pui.setBookings(pui.getPayment().getBookings()));
		return data;
	}
	
	@Override
	public PaymentUI convert(Payment entity) {
		return new PaymentUI(entity);
	}

	@Override
	protected CriteriaQuery<Payment> createCriteria(boolean addOrder) {
		CriteriaQuery<Payment> query = super.createCriteria(addOrder);
		Fetch<Payment,Member> payer = root.fetch(Payment_.payer, JoinType.LEFT);
		return query;
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
