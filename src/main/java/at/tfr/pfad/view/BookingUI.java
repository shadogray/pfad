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

import at.tfr.pfad.BookingStatus;
import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Payment;

public class BookingUI extends Booking {

	private Booking booking;
	private boolean isPayed;
	private Set<Member> payers;
	private Set<Payment> payments;

	public BookingUI(Booking booking) {
		this.booking = booking;
		this.payments = booking.getPayments();
		this.payers = booking.getPayments().stream().map(Payment::getPayer).collect(Collectors.toSet());
	}

	public Long getId() {
		return booking.getId();
	}

	public void setId(Long id) {
		booking.setId(id);
	}

	public int getVersion() {
		return booking.getVersion();
	}

	public void setVersion(int version) {
		booking.setVersion(version);
	}

	public boolean equals(Object obj) {
		return booking.equals(obj);
	}

	public int hashCode() {
		return booking.hashCode();
	}

	public Set<Payment> getPayments() {
		return payments;
	}

	public void setPayments(Set<Payment> payments) {
		this.payments = payments;
	}

	public Member getMember() {
		return booking.getMember();
	}

	public void setMember(Member member) {
		booking.setMember(member);
	}

	public Activity getActivity() {
		return booking.getActivity();
	}

	public void setActivity(Activity Activity) {
		booking.setActivity(Activity);
	}
	
	public BookingStatus getStatus() {
		return booking.getStatus();
	}

	public void setStatus(BookingStatus status) {
		booking.setStatus(status);
	}
	
	public String getComment() {
		return booking.getComment();
	}

	public void setComment(String comment) {
		booking.setComment(comment);
	}

	public Date getChanged() {
		return booking.getChanged();
	}

	public void setChanged(Date changed) {
		booking.setChanged(changed);
	}

	public Date getCreated() {
		return booking.getCreated();
	}

	public void setCreated(Date created) {
		booking.setCreated(created);
	}

	public String getChangedBy() {
		return booking.getChangedBy();
	}

	public void setChangedBy(String changedBy) {
		booking.setChangedBy(changedBy);
	}

	public String toString() {
		return booking.toString();
	}

	public boolean isPayed() {
		return payments.stream().anyMatch(p->Boolean.TRUE.equals(p.getFinished()));
	}
	
	public String getPayer() {
		return payments.stream().filter(b->b.getPayer() != null).map(Payment::getPayer).map(p->p.toString()).collect(Collectors.joining(","));
	}

	public String getSquadName() {
		if (booking.getSquad() != null) {
			return booking.getSquad().getName();
		}
		return booking.getMember() != null && booking.getMember().getTrupp() != null ? booking.getMember().getTrupp().getName() : "";
	}
}
