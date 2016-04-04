/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import at.tfr.pfad.PaymentType;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Payment;

public class PaymentUI extends Payment {

	private Payment payment;
	private Set<Booking> bookings;

	public PaymentUI(Payment payment) {
		this.payment = payment;
		this.bookings = payment.getBookings();
		bookings.size();
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
		return payment.getPayer();
	}

	public void setPayer(Member Payer) {
		payment.setPayer(Payer);
	}
	
	public String getPayerName() {
		return payment.getPayer() != null ? payment.getPayer().getName()+","+payment.getPayer().getVorname() : ""; 
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

	public String getComment() {
		return payment.getComment();
	}

	public void setComment(String comment) {
		payment.setComment(comment);
	}

	public Set<Booking> getBookings() {
		return bookings;
	}

	public void setBookings(Set<Booking> bookings) {
		this.bookings = bookings;
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
		return bookings.stream().filter(b->b.getMember() != null).map(Booking::getMember).map(m->m.toString()).collect(Collectors.joining(","));
	}

	public String getSquad() {
		return bookings.stream().filter(b->b.getMember() != null && b.getMember().getTrupp() != null).map(b->b.getMember().getTrupp().getName()).collect(Collectors.joining(","));
	}

	public String getActivity() {
		return bookings.stream().filter(b->b.getActivity() != null).map(Booking::getActivity).map(a->a.toString()).collect(Collectors.joining(","));
	}
	
	public String getActivitiesLines() {
		return bookings.stream().filter(b->b.getActivity() != null).map(b -> b.getActivity().toString()).collect(Collectors.joining("<br>"));
	}
}
