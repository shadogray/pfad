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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.primefaces.event.SelectEvent;

import at.tfr.pfad.dao.ActivityRepository;
import at.tfr.pfad.dao.BookingRepository;
import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.dao.Members;
import at.tfr.pfad.dao.ParticipationRepository;
import at.tfr.pfad.dao.PaymentRepository;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.dao.TrainingRepository;
import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Participation;
import at.tfr.pfad.model.Payment;
import at.tfr.pfad.model.Training;
import at.tfr.pfad.util.Bookings;
import at.tfr.pfad.util.Payments;
import at.tfr.pfad.util.SessionBean;
import at.tfr.pfad.util.TemplateUtils;

/**
 * @author u0x27vo
 *
 */
public abstract class BaseBean<T> implements Serializable {

	protected Logger log = Logger.getLogger(getClass());
	protected String menuItem = "base";
	
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
	protected transient Bookings bookings;
	@Inject
	protected transient Members members;
	@Inject
	protected transient Payments payments;
	@Inject
	protected transient MemberRepository memberRepo;
	@Inject
	protected transient BookingRepository bookingRepo;
	@Inject
	protected transient PaymentRepository paymentRepo;
	@Inject
	protected transient SquadRepository squadRepo;
	@Inject
	protected transient TrainingRepository trainingRepo;
	@Inject
	protected transient ParticipationRepository participationRepo;
	@Inject
	protected transient ActivityRepository activityRepo;
	@Inject
	protected PfadUI pfadUI;
	@Inject
	protected TemplateUtils templateUtils;
	
	protected int page;
	protected long count;
	protected final Map<String,String> trueOnly = new LinkedHashMap<>();
	protected final Map<String,String> falseOnly = new LinkedHashMap<>();
	protected final Map<String,String> trueFalse = new LinkedHashMap<>();

	public BaseBean() {
		trueFalse.put("Ja", Boolean.TRUE.toString());
		trueFalse.put("Nein", Boolean.FALSE.toString());
		trueOnly.put("Ja", Boolean.TRUE.toString());
		falseOnly.put("Nein", Boolean.FALSE.toString());
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
	
	public boolean isTrainer() {
		return sessionBean.isTrainer();
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
	protected String focusId;

	protected Payment payment;
	protected Booking booking;
	protected Member member;

	protected Member memberExample = new Member();
	protected Booking bookingExample = new Booking(null);
	protected Payment paymentExample = new Payment();
	protected Activity activityExample = new Activity();
	protected Training trainingExample = new Training();

	protected Member memberSearch;
	protected Booking bookingSearch;
	protected Payment paymentSearch;
	protected Activity activitySearch;
	protected Training trainingSearch;
	
	protected Member memberToAdd;
	protected Booking bookingToAdd;
	protected Payment paymentToAdd;
	protected Activity activityToAdd;
	protected Training trainingToAdd;
	protected Participation exampleParticipation = null;
	protected List<Member> filteredMembers = new ArrayList<>();
	protected List<Booking> filteredBookings = new ArrayList<>();
	protected List<Payment> filteredPayments = new ArrayList<>();
	protected T alwaysNull;

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
	
	public String getFocusId() {
		return focusId;
	}
	public void setFocusId(String focusId) {
		this.focusId = focusId;
	}
	
	public void retrieve(Long id) {
		this.id = id;
		retrieve();
	}
	
	public abstract void retrieve();
	
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
	public Activity getActivityExample() {
		return activityExample;
	}
	public void setActivityExample(Activity activityExample) {
		this.activityExample = activityExample;
	}
	public Training getTrainingExample() {
		return trainingExample;
	}
	public void setTrainingExample(Training trainingExample) {
		this.trainingExample = trainingExample;
	}
	public Payment getPayment() {
		return payment;
	}
	
	public List<Payment> getPayments(Booking b) {
		if (b == null) {
			return new ArrayList<>();
		}
		if (b.getId() == null) {
			return new ArrayList<Payment>(b.getPayments().stream().sorted((x,y) -> x.getId().compareTo(y.getId())).collect(Collectors.toList()));
		}
		return paymentRepo.findByBooking(b);
	}
	
	public void setPayment(Payment payment) {
		if (this.payment == null || !this.payment.equals(payment)) {
			this.payment = payment;
		}
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
	
	public List<Booking> getBookings(Payment p) {
		if (p == null) {
			return new ArrayList<>();
		}
		if (p.getId() == null) {
			return new ArrayList<Booking>(p.getBookings().stream().sorted((x,y) -> x.getId().compareTo(y.getId())).collect(Collectors.toList()));
		}
		return bookingRepo.findByPayment(p);
	}
	
	public Participation getExampleParticipation() {
		return exampleParticipation;
	}

	public void setExampleParticipation(Participation exampleParticipation) {
		this.exampleParticipation = exampleParticipation;
	}

	public List<Booking> filterBookings(String filter) {
		return filterBookings(null, null, filter);
	}
	
	public List<Booking> filterBookings(FacesContext facesContext, UIComponent component, final String filter) {
		if (StringUtils.isNotBlank(filter) && filter.length() < 16) {
			if (component == null) 
				filteredBookings = bookings.filtered(filter);
			else 
				filteredBookings = bookings.filtered(facesContext, component, filter);
		}
		return filteredBookings;
	}
	
	public List<Booking> getFilteredBookings() {
		return filteredBookings;
	}

	public List<Member> filterMembers(String filter) {
		return filterMembers(null, null, filter);
	}
	
	public List<Member> filterMembers(FacesContext facesContext, UIComponent component, final String filter) {
		if (StringUtils.isNotBlank(filter) && filter.length() < 16) {
			if (component == null) 
				filteredMembers = members.filtered(filter);
			else 
				filteredMembers = members.filtered(facesContext, component, filter);
		}
		return filteredMembers;
	}
	
	public List<Member> getFilteredMembers() {
		return filteredMembers;
	}
	
	public List<Payment> filterPayments(String filter) {
		return filterPayments(null, null, filter);
	}
	
	public List<Payment> filterPayments(FacesContext facesContext, UIComponent component, final String filter) {
		if (StringUtils.isNotBlank(filter) && filter.length() < 16) {
			if (component == null) 
				filteredPayments = payments.filtered(filter);
			filteredPayments = payments.filtered(facesContext, component, filter);
		}
		return filteredPayments;
	}
	
	public List<Payment> getFilteredPayments() {
		return filteredPayments;
	}

	protected Member attachMemberToAdd() {
		Member m = memberToAdd;
		try {
			if (memberToAdd != null) {
				m = findMemberById(memberToAdd.getId());
			}
		} finally {
			memberToAdd = null;
		}
		return m;
	}

	protected Booking attachBookingToAdd() {
		Booking b = bookingToAdd;
		try {
			if (bookingToAdd != null) {
				b = findBookingById(bookingToAdd.getId());
			}
		} finally {
			bookingToAdd = null;
		}
		return b;
	}

	protected Payment attachPaymentToAdd() {
		Payment p = paymentToAdd;
		try {
			if (paymentToAdd != null) {
				p = findPaymentById(paymentToAdd.getId());
			}
		} finally {
			paymentToAdd = null;
		}
		return p;
	}

	protected Activity attachActivityToAdd() {
		Activity a = activityToAdd;
		try {
			if (activityToAdd != null) {
				a = findActivityById(activityToAdd.getId());
			}
		} finally {
			activityToAdd = null;
		}
		return a;
	}

	protected Training attachTrainingToAdd() {
		Training t = trainingToAdd;
		try {
			if (trainingToAdd != null) {
				t = findTrainingById(trainingToAdd.getId());
			}
		} finally {
			trainingToAdd = null;
		}
		return t;
	}

	public void addPaymentBooking(AjaxBehaviorEvent event) {
		log.debug("addPaymentBooking: " + event);
		focusId = event.getComponent().getClientId();
		Booking b2a = attachBookingToAdd();
		if (b2a != null) {
			payment.getBookings().add(b2a);
			b2a.getPayments().add(payment);
			payment.updateType(b2a.getActivity());
		}
	}

	public void selectPaymentPayer(AjaxBehaviorEvent event) {
		log.debug("selectPaymentPayer: " + event);
		focusId = event.getComponent().getClientId();
		payment.setPayer(attachMemberToAdd());
	}

	public void selectBookingMember(AjaxBehaviorEvent event) {
		log.debug("selectBookingMember: " + event);
		focusId = event.getComponent().getClientId();
		booking.setMember(attachMemberToAdd());
	}

	public void selectMemberVollzahler(AjaxBehaviorEvent event) {
		log.debug("selectMemberVollzahler: " + event);
		focusId = event.getComponent().getClientId();
		member.setVollzahler(attachMemberToAdd());
	}

	public void addMemberSibling(SelectEvent event) {
		log.debug("selectMemberSibling: " + event);
		focusId = event.getComponent().getClientId();
		Member m2a = attachMemberToAdd();
		if (m2a != null) {
			if (m2a.equals(member) || m2a.getSiblings().contains(member)) {
				throw new IllegalArgumentException("Cannot add Parent as Child: parent="+member+", childToAdd: "+m2a);
			}
			member.getSiblings().add(m2a);
		}
	}

	public Booking findBookingById(Long id) {
		return this.entityManager.find(Booking.class, id);
	}

	public Member findMemberById(Long id) {
		return this.entityManager.find(Member.class, id);
	}
	
	public Payment findPaymentById(Long id) {
		return this.entityManager.find(Payment.class, id);
	}
	
	public Activity findActivityById(Long id) {
		return this.entityManager.find(Activity.class, id);
	}
	
	public Training findTrainingById(Long id) {
		return this.entityManager.find(Training.class, id);
	}

	public Participation findParticipationById(Long id) {
		return this.entityManager.find(Participation.class, id);
	}

	
	public Booking getBookingToAdd() {
		return bookingToAdd;
	}
	public void setBookingToAdd(Booking bookingToAdd) {
		this.bookingToAdd = bookingToAdd;
	}

	public Member getMemberSearch() {
		return memberSearch;
	}

	public void setMemberSearch(Member memberSearch) {
		this.memberSearch = memberSearch;
	}

	public Booking getBookingSearch() {
		return bookingSearch;
	}

	public void setBookingSearch(Booking bookingSearch) {
		this.bookingSearch = bookingSearch;
	}

	public Payment getPaymentSearch() {
		return paymentSearch;
	}

	public void setPaymentSearch(Payment paymentSearch) {
		this.paymentSearch = paymentSearch;
	}

	public Activity getActivitySearch() {
		return activitySearch;
	}

	public void setActivitySearch(Activity activitySearch) {
		this.activitySearch = activitySearch;
	}

	public Training getTrainingSearch() {
		return trainingSearch;
	}

	public void setTrainingSearch(Training trainingSearch) {
		this.trainingSearch = trainingSearch;
	}

	public Member getMemberToAdd() {
		return memberToAdd;
	}
	public void setMemberToAdd(Member memberToAdd) {
		this.memberToAdd = memberToAdd;
	}

	public Payment getPaymentToAdd() {
		return paymentToAdd;
	}
	public void setPaymentToAdd(Payment paymentToAdd) {
		this.paymentToAdd = paymentToAdd;
	}
	
	public Activity getActivityToAdd() {
		return activityToAdd;
	}
	
	public void setActivityToAdd(Activity activityToAdd) {
		this.activityToAdd = activityToAdd;
	}
	
	public Training getTrainingToAdd() {
		return trainingToAdd;
	}
	
	public void setTrainingToAdd(Training trainingToAdd) {
		this.trainingToAdd = trainingToAdd;
	}
	
	public String getNullString() {
		return null;
	}
	
	public void setNullString(String any) {}
	
	public SessionContext getSessionContext() {
		return sessionContext;
	}
	
	public T getAlwaysNull() {
		return alwaysNull;
	}
	
	public void setAlwaysNull(T alwaysNull) {
		//this.alwaysNull = alwaysNull;
	}
	
	public void info(String message) {
		info(null, message);
	}
	
	public void info(String id, String message) {
		uiMessage(id, FacesMessage.SEVERITY_INFO, message, null);
	}

	public void warn(String message) {
		warn(null, message);
	}
	
	public void warn(String id, String message) {
		uiMessage(id, FacesMessage.SEVERITY_WARN, message, null);
	}

	public void error(String message) {
		error(null, message);
	}
	
	public void error(String id, String message) {
		uiMessage(id, FacesMessage.SEVERITY_ERROR, message, null);
	}

	public void uiMessage(String id, Severity severity, String message, String detail) {
		FacesContext.getCurrentInstance().addMessage(id, new FacesMessage(severity, message, detail));
	}
}