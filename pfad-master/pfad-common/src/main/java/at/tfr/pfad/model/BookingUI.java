/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.model;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import at.tfr.pfad.BookingStatus;

public class BookingUI extends Booking {

	private Booking booking;
	private Member member;
	private Activity activity;
	private Squad squad;
	private boolean free;
	private Set<Payment> payments;
	private String squadName;
	boolean finished;
	boolean aconto;


	public BookingUI(Booking booking) {
		this(booking, booking.getMember(), booking.getActivity(), booking.getSquad());
	}
	
	public BookingUI(Booking booking, Member member, Activity activity, Squad squad) {
		this.booking = booking;
		this.member = member;
		this.activity = activity;
		this.squad = squad;
		this.payments = booking.getPayments();
		if (member != null) {
			free = member.isFree() || 
					member.getFunktionen().stream().anyMatch(f->Boolean.TRUE.equals(f.getFree()));
		}
		squadName = member != null && member.getTrupp() != null ? member.getTrupp().getName() : null;
		if (squadName == null) {
			if (booking.getSquad() != null) {
				squadName = booking.getSquad().getName();
			}
		}
		finished = payments.stream().anyMatch(p->Boolean.TRUE.equals(p.getFinished()));
		aconto = payments.stream().anyMatch(p->Boolean.TRUE.equals(p.getAconto()));
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
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
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

	public String getPayed() {
		if (finished) {
			return "JA";
		}
		if (aconto) {
			return "ANZ";
		}
		return "NEIN";
	}
	
	public String getPayer() {
		return payments.stream().filter(b->b.getPayer() != null).map(Payment::getPayer).map(p->p.toString()).collect(Collectors.joining(","));
	}
	
	@Override
	public Squad getSquad() {
		return squad;
	}

	public String getSquadName() {
		return squadName;
	}
	
	public String getStrasse() {
		return booking.getMember().getStrasse();
	}
	
	public String getOrt() {
		return booking.getMember().getOrt();
	}
	
	public boolean isFree() {
		return free;
	}
	
	public void setFree(boolean free) {
		this.free = free;
	}
}
