/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.persistence.FetchType;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.apache.commons.lang3.StringUtils;

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
public class PaymentDataModel extends DataModel<Payment, PaymentUI> {

	public PaymentDataModel() {
		uiClass = PaymentUI.class;
		entityClass = Payment.class;
	}

	public PaymentDataModel(Class<PaymentUI> uiClass, Class<Payment> entityClass) {
		super(uiClass, entityClass);
	}

	@Override
	protected Path[] getGroupByRoots() {
		return new Path[] { root };
	}
	
	@Override
	public List<PaymentUI> convertToUiBean(List<Payment> list) {
		if (!list.isEmpty()) {
			CriteriaQuery<Tuple> cq = cb.createTupleQuery();
			Root<Payment> pr = cq.from(Payment.class);
			Join<Payment,Booking> bj = pr.join(Payment_.bookings, JoinType.LEFT);
			bj.alias("bookings");
			Join<Booking,Member> mj = bj.join(Booking_.member);
			mj.alias("member");
			Join<Member,Squad> sj = mj.join(Member_.trupp, JoinType.LEFT);
			sj.alias("trupp");

			cq.multiselect(pr, bj, mj);
			
			pr.fetch(Payment_.payer);
			cq.where(pr.in(list));
			cq.orderBy(createOrders(pr));
			cq.distinct(true);
			
			List<Tuple> res = entityManager.createQuery(cq).getResultList();
			
			return res.stream().map(t->new PaymentUI(t.get(0, Payment.class))).collect(Collectors.toList());
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
		case "payer":
			return getSplittedPredicateName(root.join(Payment_.payer), val);
		
		case "member":
			return getSplittedPredicateName(root.join(Payment_.bookings).join(Booking_.member), val);
		
		case "squad":
			return cb.like(cb.lower(root.join(Payment_.bookings).join(Booking_.member).join(Member_.trupp).get(Squad_.name)), "%"+val+"%");

		case "activity":
			return cb.like(cb.lower(root.join(Payment_.bookings).join(Booking_.activity).get(Activity_.name)), "%"+val+"%");
		}
		return super.createFilterCriteriaForField(propertyName, val, criteriaQuery);
	}

	@Override
	protected Path getPathForOrder(Root<Payment> path, String propertyName) {
		switch(propertyName) {
		case "payer":
			return path.get(Payment_.payer).get(Member_.name);
		
		case "member":
			return path.<Member>get("member").get(Member_.name);
		
		case "squad":
			return path.<Squad>get("trupp").get(Squad_.name);

		case "activity":
			return path.join(Payment_.bookings).join(Booking_.activity).get(Activity_.name);
		}
		return super.getPathForOrder(path, propertyName);
	}
}
