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
import javax.enterprise.context.RequestScoped;
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

	private SetJoin<Payment, Booking> joinBookings;
	private Join<Booking,Activity> joinActivity;
	private Join<Booking, Member> joinMember;
	private Join<Member,Squad> joinTrupp;
	private Join<Payment, Member> joinPayer;
	
	public PaymentDataModel() {
		uiClass = PaymentUI.class;
		entityClass = Payment.class;
	}

	public PaymentDataModel(Class<PaymentUI> uiClass, Class<Payment> entityClass) {
		super(uiClass, entityClass);
	}

	@Override
	protected Root<Payment> getRoot() {
		Root<Payment> root = super.getRoot();
		joinBookings = root.join(Payment_.bookings, JoinType.INNER);
		joinActivity = joinBookings.join(Booking_.activity, JoinType.INNER);
		joinMember = joinBookings.join(Booking_.member, JoinType.INNER);
		joinTrupp = joinMember.join(Member_.trupp, JoinType.LEFT);
		joinPayer = root.join(Payment_.payer, JoinType.LEFT);
		return root;
	}

	@Override
	protected Root<Payment> getCountRoot() {
		Root<Payment> root = super.getCountRoot();
		joinBookings = root.join(Payment_.bookings, JoinType.INNER);
		joinActivity = joinBookings.join(Booking_.activity, JoinType.INNER);
		joinMember = joinBookings.join(Booking_.member, JoinType.INNER);
		joinTrupp = joinMember.join(Member_.trupp, JoinType.LEFT);
		joinPayer = root.join(Payment_.payer, JoinType.LEFT);
		return root;
	}
	
	@Override
	protected Path[] getGroupByRoots() {
		return new Path[] { root, joinBookings, joinMember};
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
			return getSplittedPredicateName(joinPayer, val);
		
		case "member":
			return getSplittedPredicateName(joinMember, val);
		
		case "squad":
			return cb.like(cb.lower(joinTrupp.get(Squad_.name)), "%"+val+"%");

		case "activity":
			return cb.like(cb.lower(joinActivity.get(Activity_.name)), "%"+val+"%");
		}
		return super.createFilterCriteriaForField(propertyName, val, criteriaQuery);
	}

	@Override
	protected Path getPathForOrder(String propertyName) {
		switch(propertyName) {
		case "payer":
			return joinPayer.get(Member_.name);
		
		case "member":
			return joinMember.get(Member_.name);
		
		case "squad":
			return joinMember.get(Member_.trupp).get(Squad_.name);

		case "activity":
			return joinActivity.get(Activity_.name);
		}
		return super.getPathForOrder(propertyName);
	}
}
