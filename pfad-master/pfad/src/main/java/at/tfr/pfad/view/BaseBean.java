/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.primefaces.event.SelectEvent;

import at.tfr.pfad.dao.BookingRepository;
import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.dao.Members;
import at.tfr.pfad.dao.PaymentRepository;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Payment;
import at.tfr.pfad.svc.BookingDao;
import at.tfr.pfad.svc.BookingMapper;
import at.tfr.pfad.svc.MemberDao;
import at.tfr.pfad.svc.MemberMapper;
import at.tfr.pfad.svc.PaymentDao;
import at.tfr.pfad.svc.PaymentMapper;
import at.tfr.pfad.svc.SquadMapper;

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
	@Inject
	private MemberRepository memberRepo;
	@Inject
	protected BookingRepository bookingRepo;
	@Inject
	protected PaymentRepository paymentRepo;
	@Inject
	protected SquadRepository squadRepo;
	
	@Inject
	protected PaymentMapper paymentMap;
	@Inject
	protected BookingMapper bookingMap;
	@Inject
	protected MemberMapper memberMap;
	@Inject
	protected SquadMapper squadMap;
	
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
	protected List<MemberDao> filteredMembers = new ArrayList<>();
	protected List<MemberDao> filteredPayers = new ArrayList<>();
	protected List<BookingDao> filteredBookings = new ArrayList<>();
	protected List<PaymentDao> filteredPayments = new ArrayList<>();

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

	public List<Payment> getPayments(Booking b) {
		if (b == null) {
			return Collections.emptyList();
		}
		if (b.getId() == null) {
			return new ArrayList<Payment>(b.getPayments().stream().sorted((x,y) -> x.getId().compareTo(y.getId())).collect(Collectors.toList()));
		}
		return paymentRepo.findByBooking(b);
	}
	
	public List<Booking> getBookings(Payment p) {
		if (p == null) {
			return Collections.emptyList();
		}
		if (p.getId() == null) {
			return new ArrayList<Booking>(p.getBookings().stream().sorted((x,y) -> x.getId().compareTo(y.getId())).collect(Collectors.toList()));
		}
		return bookingRepo.findByPayment(p);
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
	
	public List<BookingDao> filterBookings(final String filter) {
		if (StringUtils.isNotBlank(filter) && filter.length() < 16) {
			filteredBookings = bookings.filtered(filter)
					.stream().map(b->bookingMap.toDao(b)).collect(Collectors.toList());
		}
		return filteredBookings;
	}
	
	public List<BookingDao> getFilteredBookings() {
		return filteredBookings;
	}

	public List<MemberDao> filterPayers(final String filter) {
		if (StringUtils.isNotBlank(filter) && filter.length() < 16) {
			filteredPayers = members.filtered(filter)
					.stream().map(b->memberMap.toDao(b)).collect(Collectors.toList());
		}
		return filteredPayers;
	}
	
	public List<MemberDao> getFilteredPayers() {
		return filteredPayers;
	}

	public List<MemberDao> filterMembers(final String filter) {
		if (StringUtils.isNotBlank(filter) && filter.length() < 16) {
			filteredMembers = members.filtered(filter)
					.stream().map(b->memberMap.toDao(b)).collect(Collectors.toList());
		}
		return filteredMembers;
	}
	
	public List<MemberDao> getFilteredMembers() {
		return filteredMembers;
	}
	
	public List<PaymentDao> filterPayments(final String filter) {
		if (StringUtils.isNotBlank(filter) && filter.length() < 16) {
			filteredPayments = payments.filtered(filter)
					.stream().map(b->paymentMap.toDao(b)).collect(Collectors.toList());
		}
		return filteredPayments;
	}
	
	public List<PaymentDao> getFilteredPayments() {
		return filteredPayments;
	}

	public Booking selectBooking(SelectEvent event) {
		log.debug("selectBooking: " + event);
		Object val = event.getObject();
		if (!(val instanceof BookingDao)) {
			return null;
		} else {
			return findBookingById(((BookingDao)val).getId());
		}
	}

	public Payment selectPayment(SelectEvent event) {
		log.debug("selectPayment: " + event);
		Object val = event.getObject();
		if (!(val instanceof Payment)) {
			return null;
		} else {
			return findPaymentById(((Payment)val).getId());
		}
	}

	public Member selectMember(SelectEvent event) {
		log.debug("selectPayment: " + event);
		Object val = event.getObject();
		if (!(val instanceof Member)) {
			return null;
		} else {
			return findMemberById(((Member)val).getId());
		}
	}

	public void selectExampleBooking(SelectEvent event) {
		log.debug("selectExampleBooking: " + event);
		Object val = event.getObject();
		if (!(val instanceof Booking)) {
			exampleBooking = null;
		} else {
			exampleBooking = findBookingById(((Booking)val).getId());
			filteredBookings.add(bookingMap.toDao(exampleBooking));
		}
	}

	public void selectExamplePayer(SelectEvent event) {
		log.debug("selectExamplePayer: " + event);
		Object val = event.getObject();
		if (!(val instanceof Member)) {
			examplePayer = null;
		} else {
			examplePayer = findMemberById(Long.valueOf(((Member)val).getId()));
			filteredPayers.add(memberMap.toDao(examplePayer));
		}
	}

	public void selectExampleMember(SelectEvent event) {
		log.debug("selectExampleMember: " + event);
		Object val = event.getObject();
		if (!(val instanceof Member)) {
			exampleMember = null;
		} else {
			exampleMember = findMemberById(Long.valueOf(((Member)val).getId()));
			filteredMembers.add(memberMap.toDao(exampleMember));
		}
	}

	public Booking findBookingById(Long id) {
		return bookingRepo.findById(id);
	}

	public Payment findPaymentById(Long id) {
		return paymentRepo.findById(id);
	}

	public Member findMemberById(Long id) {
		return memberRepo.findById(id);
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