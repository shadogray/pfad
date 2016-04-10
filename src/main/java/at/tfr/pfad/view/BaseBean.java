/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.richfaces.component.UISelect;

import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Payment;

public abstract class BaseBean implements Serializable {

	protected Logger log = Logger.getLogger(getClass());
	
	@Inject
	protected transient EntityManager entityManager;
	@Resource
	protected SessionContext sessionContext;
	@Inject
	protected SessionBean sessionBean;
	@Inject
	protected Validator validator;
	@Inject
	protected PageSizeBean pageSize;
	@Inject
	private Bookings bookings;
	@Inject
	private Members members;
	@Inject
	private Payments payments;
	
	protected int page;
	protected long count;

	public BaseBean() {
		super();
	}
	
	@PostConstruct
	public void init() {
		log.debug("creating: "+sessionContext+" : "+Thread.currentThread()+" : "+this);
	}

	public boolean isAdmin() {
		return sessionBean.isAdmin();
	}
	
	public boolean isGruppe() {
		return sessionBean.isGruppe();
	}

	public boolean isLeiter() {
		return sessionBean.isLeiter();
	}

	public boolean isKassier() {
		return sessionBean.isKassier();
	}

	public boolean isVorstand() {
		return sessionBean.isVorstand();
	}

	public boolean isViewAllowed() {
		return true;
	}
	public abstract boolean isUpdateAllowed();
	public boolean isDeleteAllowed() {
		return isAdmin();
	}
	
	public boolean isRegistrationEnd() {
		return sessionBean.getRegistrationEndDate() != null && sessionBean.getRegistrationEndDate().before(new Date());
	}
	
	public int getPageSize() {
		return pageSize.getPageSize();
	}
	
	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public long getCount() {
		return this.count;
	}
	
	
	protected Long id;

	protected Payment payment;
	protected Booking booking;
	protected Member member;

	private Payment paymentExample = new Payment();
	private Booking bookingExample = new Booking();
	private Member memberExample = new Member();
	
	protected Booking bookingToAdd;
	protected Member memberToAdd;
	protected Payment paymentToAdd;
	protected Activity exampleActivity = null;
	protected Booking exampleBooking = null;
	protected Member exampleMember = null;
	protected Payment examplePayment = null;
	protected Member examplePayer = null;
	protected Member paymentPayer = null;
	protected List<Member> filteredMembers = new ArrayList<>();
	protected List<Member> filteredPayers = new ArrayList<>();
	protected List<Booking> filteredBookings = new ArrayList<>();
	protected List<Payment> filteredPayments = new ArrayList<>();

	{
		log.debug("inited");
//		bookingExample.setMember(new Member());
//		paymentExample.setPayer(new Member());
	}
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Member getMemberExample() {
		return memberExample;
	}
	public void setMemberExample(Member memberExample) {
		this.memberExample = memberExample;
	}
	public Booking getBookingExample() {
		return bookingExample;
	}
	public void setBookingExample(Booking bookingExample) {
		this.bookingExample = bookingExample;
	}
	public Payment getPaymentExample() {
		return paymentExample;
	}
	public void setPaymentExample(Payment paymentExample) {
		this.paymentExample = paymentExample;
	}
	public Payment getPayment() {
		return payment;
	}
	
	public void setPayment(Payment payment) {
		this.payment = payment;
	}
	
	public Member getMember() {
		return member;
	}
	
	public void setMember(Member member) {
		this.member = member;
	}
	
	public Booking getBooking() {
		return booking;
	}
	
	public void setBooking(Booking booking) {
		this.booking = booking;
	}
	
	public Booking getExampleBooking() {
		return exampleBooking;
	}

	public void setExampleBooking(Booking exampleBooking) {
		this.exampleBooking = exampleBooking;
	}

	public Activity getExampleActivity() {
		return exampleActivity;
	}

	public void setExampleActivity(Activity exampleActivity) {
		this.exampleActivity = exampleActivity;
	}

	public Member getExampleMember() {
		return exampleMember;
	}

	public void setExampleMember(Member exampleMember) {
		this.exampleMember = exampleMember;
	}

	public Payment getExamplePayment() {
		return examplePayment;
	}

	public void setExamplePayment(Payment examplePayment) {
		this.examplePayment = examplePayment;
	}
	
	public Member getExamplePayer() {
		return examplePayer;
	}
	
	public void setExamplePayer(Member examplePayer) {
		this.examplePayer = examplePayer;
	}
	
	public Member getPaymentPayer() {
		return paymentPayer;
	}
	
	public void setPaymentPayer(Member paymentPayer) {
		this.paymentPayer = paymentPayer;
	}
	
	public List<Booking> filterBookings(FacesContext facesContext, UIComponent component, final String filter) {
		if (StringUtils.isNotBlank(filter) && filter.length() < 16) {
			filteredBookings = bookings.filtered(facesContext, component, filter);
		}
		return filteredBookings;
	}
	
	public List<Booking> getFilteredBookings() {
		return filteredBookings;
	}

	public List<Member> filterPayers(FacesContext facesContext, UIComponent component, final String filter) {
		if (StringUtils.isNotBlank(filter) && filter.length() < 16) {
			filteredPayers = members.filtered(facesContext, component, filter);
		}
		return filteredPayers;
	}
	
	public List<Member> getFilteredPayers() {
		return filteredPayers;
	}

	public List<Member> filterMembers(FacesContext facesContext, UIComponent component, final String filter) {
		if (StringUtils.isNotBlank(filter) && filter.length() < 16) {
			filteredMembers = members.filtered(facesContext, component, filter);
		}
		return filteredMembers;
	}
	
	public List<Member> getFilteredMembers() {
		return filteredMembers;
	}
	
	public List<Payment> filterPayments(FacesContext facesContext, UIComponent component, final String filter) {
		if (StringUtils.isNotBlank(filter) && filter.length() < 16) {
			filteredPayments = payments.filtered(facesContext, component, filter);
		}
		return filteredPayments;
	}
	
	public List<Payment> getFilteredPayments() {
		return filteredPayments;
	}

	public void addPaymentBooking(AjaxBehaviorEvent event) {
		log.debug("addPaymentBooking: " + event);
		UISelect uiSelect = (UISelect) event.getSource();
		String val = (String)uiSelect.getSubmittedValue();
		if (StringUtils.isBlank(val)) {
			bookingToAdd = null;
		} else {
			bookingToAdd = findBookingById(Long.valueOf(val));
			payment.getBookings().add(bookingToAdd);
			payment.updateType(bookingToAdd.getActivity());
			uiSelect.setSubmittedValue("");
			uiSelect.setValue(null);
			uiSelect.setLocalValueSet(false);
		}
	}

	public void selectExampleBooking(AjaxBehaviorEvent event) {
		log.debug("selectExampleBooking: " + event);
		String val = (String)((UISelect) event.getSource()).getSubmittedValue();
		if (StringUtils.isBlank(val)) {
			exampleBooking = null;
		} else {
			exampleBooking = findBookingById(Long.valueOf(val));
			filteredBookings.add(exampleBooking);
		}
	}

	public void selectPaymentPayer(AjaxBehaviorEvent event) {
		log.debug("selectPaymentPayer: " + event);
		String val = (String)((UISelect) event.getSource()).getSubmittedValue();
		if (StringUtils.isBlank(val)) {
			paymentPayer = null;
		} else {
			paymentPayer = findMemberById(Long.valueOf(val));
			payment.setPayer(paymentPayer);
		}
	}

	public void selectExamplePayer(AjaxBehaviorEvent event) {
		log.debug("selectExamplePayer: " + event);
		String val = (String)((UISelect) event.getSource()).getSubmittedValue();
		if (StringUtils.isBlank(val)) {
			examplePayer = null;
		} else {
			examplePayer = findMemberById(Long.valueOf(val));
			filteredPayers.add(examplePayer);
		}
	}

	public void selectExampleMember(AjaxBehaviorEvent event) {
		log.debug("selectExampleMember: " + event);
		String val = (String)((UISelect) event.getSource()).getSubmittedValue();
		if (StringUtils.isBlank(val)) {
			exampleMember = null;
		} else {
			exampleMember = findMemberById(Long.valueOf(val));
			filteredMembers.add(exampleMember);
		}
	}

	public void selectBookingMember(AjaxBehaviorEvent event) {
		log.debug("selectBookingMember: " + event);
		String val = (String)((UISelect) event.getSource()).getSubmittedValue();
		if (StringUtils.isBlank(val)) {
			booking.setMember(null);
		} else {
			booking.setMember(findMemberById(Long.valueOf(val)));
		}
	}

	public void selectMemberVollzahler(AjaxBehaviorEvent event) {
		log.debug("selectMemberVollzahler: " + event);
		String val = (String)((UISelect) event.getSource()).getSubmittedValue();
		if (StringUtils.isBlank(val)) {
			member.setVollzahler(null);
		} else {
			member.setVollzahler(findMemberById(Long.valueOf(val)));
		}
	}

	public void addMemberSibling(AjaxBehaviorEvent event) {
		log.debug("selectMemberSibling: " + event);
		UISelect uiSelect = (UISelect) event.getSource();
		String val = (String)uiSelect.getSubmittedValue();
		if (StringUtils.isBlank(val)) {
			memberToAdd = null;
		} else {
			memberToAdd = findMemberById(Long.valueOf(val));
			member.getSiblings().add(memberToAdd);
			uiSelect.setSubmittedValue("");
			uiSelect.setValue(null);
			uiSelect.setLocalValueSet(false);
		}
	}

	public Booking findBookingById(Long id) {
		return this.entityManager.find(Booking.class, id);
	}

	public Member findMemberById(Long id) {
		return this.entityManager.find(Member.class, id);
	}
	
	public Booking getBookingToAdd() {
		return bookingToAdd;
	}
	public void setBookingToAdd(Booking bookingToAdd) {
		this.bookingToAdd = bookingToAdd;
	}

	public Member getMemberToAdd() {
		if (memberToAdd == null) {
			memberToAdd = new Member();
		}
		return memberToAdd;
	}
	public void setMemberToAdd(Member memberToAdd) {
		this.memberToAdd = memberToAdd;
	}

	public Payment getPaymentToAdd() {
		if (paymentToAdd == null) {
			paymentToAdd = new Payment();
		}
		return paymentToAdd;
	}
	public void setPaymentToAdd(Payment paymentToAdd) {
		this.paymentToAdd = paymentToAdd;
	}
	
	public String getNullString() {
		return null;
	}
	
	public void setNullString(String any) {}
	
	public SessionContext getSessionContext() {
		return sessionContext;
	}
}