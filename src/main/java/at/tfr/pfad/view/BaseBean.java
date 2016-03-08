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

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Observes;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.PhaseEvent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.jsf.api.listener.phase.BeforePhase;
import org.apache.deltaspike.jsf.api.listener.phase.JsfPhaseId;
import org.jboss.logging.Logger;
import org.richfaces.component.UISelect;

import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Payment;

public abstract class BaseBean implements Serializable {

	protected Logger log = Logger.getLogger(getClass());
	
	@Inject
	protected transient Conversation conversation;
	@Inject
	@ConversationScoped
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
	
	public Conversation getConversation() {
		return conversation;
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

	protected Payment paymentExample = new Payment();
	protected Booking bookingExample = new Booking();
	protected Member memberExample = new Member();
	
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
//		bookingExample.setMember(new Member());
//		paymentExample.setPayer(new Member());
	}
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public void selectPaymentBooking(AjaxBehaviorEvent event) {
		log.debug("addPaymentBooking: " + event);
		String val = (String)((UISelect) event.getSource()).getSubmittedValue();
		if (StringUtils.isBlank(val)) {
			bookingToAdd = null;
		} else {
			bookingToAdd = findBookingById(Long.valueOf(val));
		}
		payment.getBookings().add(bookingToAdd);
	}

	public void selectExampleBooking(AjaxBehaviorEvent event) {
		log.debug("selectExampleBooking: " + event);
		String val = (String)((UISelect) event.getSource()).getSubmittedValue();
		if (StringUtils.isBlank(val)) {
			exampleBooking = new Booking();
		} else {
			exampleBooking = findBookingById(Long.valueOf(val));
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

	public void selectPaymentExamplePayer(AjaxBehaviorEvent event) {
		log.debug("selectPaymentExamplePayer: " + event);
		String val = (String)((UISelect) event.getSource()).getSubmittedValue();
		if (StringUtils.isBlank(val)) {
			paymentExample.setPayer(null);
		} else {
			paymentExample.setPayer(findMemberById(Long.valueOf(val)));
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

	public void selectBookingExampleMember(AjaxBehaviorEvent event) {
		log.debug("selectBookingExampleMember: " + event);
		String val = (String)((UISelect) event.getSource()).getSubmittedValue();
		if (StringUtils.isBlank(val)) {
			bookingExample.setMember(null);
		} else {
			bookingExample.setMember(findMemberById(Long.valueOf(val)));
		}
	}

	public void selectExampleMember(AjaxBehaviorEvent event) {
		log.debug("selectExampleMember: " + event);
		String val = (String)((UISelect) event.getSource()).getSubmittedValue();
		if (StringUtils.isBlank(val)) {
			memberExample = new Member();
		} else {
			memberExample = findMemberById(Long.valueOf(val));
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

	public void selectMemberSibling(AjaxBehaviorEvent event) {
		log.debug("selectMemberSibling: " + event);
		String val = (String)((UISelect) event.getSource()).getSubmittedValue();
		if (StringUtils.isBlank(val)) {
			memberToAdd = null;
		} else {
			memberToAdd = findMemberById(Long.valueOf(val));
		}
		member.getSiblings().add(memberToAdd);
	}

	public Booking findBookingById(Long id) {
		return this.entityManager.find(Booking.class, id);
	}

	public Member findMemberById(Long id) {
		return this.entityManager.find(Member.class, id);
	}
	
	public Booking getBookingToAdd() {
		if (bookingToAdd == null) {
			bookingToAdd = new Booking();
		}
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
	
	public void prepareRender(@Observes @BeforePhase(JsfPhaseId.RENDER_RESPONSE) PhaseEvent event) {
		if (payment != null && bookingToAdd != null && payment.getBookings().contains(bookingToAdd)) {
			bookingToAdd = new Booking();
		}
		if (member != null && memberToAdd != null && member.getSiblings().contains(memberToAdd)) {
			memberToAdd = new Member();
		}
	}

	public String getNullString() {
		return null;
	}
	
	public void setNullString(String any) {}
}