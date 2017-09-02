/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.model;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import at.tfr.pfad.PaymentType;

public class PaymentUI extends Payment {

	private Payment payment;
	private Member payer;
	private Set<Booking> bookings;
	private String members;
	private String squads;
	private String activities;
	private String activitiesLines;
	private String payerName;

	public PaymentUI(Payment payment) {
		this(payment, payment.getPayer());
	}
	
	public PaymentUI(Payment payment, Member payer) {
		this.payment = payment;
		this.payer = payer;
		payerName = payer != null ? payer.getName()+","+payer.getVorname() : "";
	}

	public Payment getPayment() {
		return payment;
	}
	
	public Long getId() {
		return payment.getId();
	}

	public void setId(Long id) {
		payment.setId(id);
	}

	public int getVersion() {
		return payment.getVersion();
	}

	public void setVersion(int version) {
		payment.setVersion(version);
	}

	public boolean equals(Object obj) {
		return payment.equals(obj);
	}

	public int hashCode() {
		return payment.hashCode();
	}

	public Member getPayer() {
		return payer;
	}

	public void setPayer(Member payer) {
		this.payer = payer;
	}
	
	public String getPayerName() {
		return payerName; 
	}
	
	@Override
	public Float getAmount() {
		return payment.getAmount();
	}
	
	@Override
	public void setAmount(Float amount) {
		payment.setAmount(amount);
	}

	public Boolean getFinished() {
		return payment.getFinished();
	}

	public void setFinished(Boolean finished) {
		payment.setFinished(finished);
	}
	
	public Boolean getAconto() {
		return payment.getAconto();
	}
	
	public void setAconto(Boolean aconto) {
		payment.setAconto(aconto);
	}

	public String getComment() {
		return payment.getComment();
	}

	public void setComment(String comment) {
		payment.setComment(comment);
	}
	
	public String getPayerIBAN() {
		return payment.getPayerIBAN();
	}

	public void setPayerIBAN(String payerIBAN) {
		payment.setPayerIBAN(payerIBAN);
	}
	
	public Set<Booking> getBookings() {
		return bookings;
	}

	public void setBookings(Set<Booking> bookings) {
		this.bookings = bookings;
		members = bookings.stream().filter(b->b.getMember() != null).map(Booking::getMember).map(m->m.toString()).collect(Collectors.joining(","));
		squads = bookings.stream().filter(b->b.getMember() != null && b.getMember().getTrupp() != null).map(b->b.getMember().getTrupp().getName()).collect(Collectors.joining(","));
		activities = bookings.stream().filter(b->b.getActivity() != null).map(Booking::getActivity).map(a->a.toString()).collect(Collectors.joining(","));
		activitiesLines = bookings.stream().filter(b->b.getActivity() != null).map(b -> b.getActivity().toString()).collect(Collectors.joining("<br>"));
	}

	public PaymentType getType() {
		return payment.getType();
	}

	public void setType(PaymentType type) {
		payment.setType(type);
	}

	public Date getPaymentDate() {
		return payment.getPaymentDate();
	}

	public void setPaymentDate(Date paymentDate) {
		payment.setPaymentDate(paymentDate);
	}

	public Date getChanged() {
		return payment.getChanged();
	}

	public void setChanged(Date changed) {
		payment.setChanged(changed);
	}

	public Date getCreated() {
		return payment.getCreated();
	}

	public void setCreated(Date created) {
		payment.setCreated(created);
	}

	public String getChangedBy() {
		return payment.getChangedBy();
	}

	public void setChangedBy(String changedBy) {
		payment.setChangedBy(changedBy);
	}

	public String getCreatedBy() {
		return payment.getCreatedBy();
	}

	public void setCreatedBy(String createdBy) {
		payment.setCreatedBy(createdBy);
	}

	public String toString() {
		return payment.toString();
	}
	
	public String getBooking() {
		return bookings.toString();
	}
	
	public String getMember() {
		return members;
	}

	public String getSquad() {
		return squads;
	}

	public String getActivity() {
		return activities;
	}
	
	public String getActivitiesLines() {
		return activitiesLines;
	}
}
