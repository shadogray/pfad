package at.tfr.pfad.view;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateful;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.event.SelectEvent;

import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Payment;
import at.tfr.pfad.svc.BookingDao;
import at.tfr.pfad.svc.PaymentDao;

@Named
@ViewScoped
@Stateful
public class PaymentEditBean extends BaseBean {

	@Inject
	private PaymentBean paymentBean;
	
	private List<PaymentDao> relatedPayments = Collections.emptyList();
	private List<BookingDao> bookings = Collections.emptyList();
	private boolean paymentPopupVisible;
	
	public Payment initialize(Long id) {
		this.id = id;
		paymentBean.setId(id);
		paymentBean.retrieve();
		updateUI();
		return paymentBean.getPayment();
	}
	
	public Payment save() {
		paymentBean.update();
		return initialize(paymentBean.getPayment().getId());
	}

	public void updateUI() {
		bookings = paymentBean.getPayment().getBookings().stream()
				.map(b->bookingMap.toDao(b)).collect(Collectors.toList());
		if (bookings.isEmpty()) {
			relatedPayments = Collections.emptyList();
		} else {
			BookingDao b = bookings.iterator().next();
			relatedPayments = paymentRepo.findByBookingIdOrderByIdDesc(b.getId()).stream()
					.map(p -> paymentMap.toDao(p)).collect(Collectors.toList());
		}
	}

	public void addSelectedBooking(SelectEvent event) {
		Booking b = selectBooking(event);
		if (b != null) {
			addBooking(b.getId());
		}
	}
	
	public void addBooking(Long bookingId) {
		Booking booking = bookingRepo.findById(bookingId);
		paymentBean.getPayment().getBookings().add(booking);
		updateUI();
		((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest()).removeAttribute("selectedBooking");
	}
	
	public void removeBooking(Long bookingId) {
		paymentBean.getPayment().getBookings().removeIf(b->b.getId().equals(bookingId));
		updateUI();
	}
	
	public void forBooking(AjaxBehaviorEvent event) {
		UIInput input = (UIInput)event.getSource();
		Object value = input.getValue();
		if (value instanceof Booking) {
			forBooking(((Booking)value).getId());
		} else {
			forBooking(Long.valueOf(""+value));
		}
	}
	
	public void forBooking(Long id) {
		Booking booking = bookingRepo.findById(id);
		Long paymentId = null;
		if (!booking.getPayments().isEmpty()) {
			paymentId = booking.getPayments().iterator().next().getId();
		}
		initialize(paymentId);
		if (paymentBean.getPayment().getId() == null) {
			paymentBean.getPayment().updateType(booking.getActivity());
			paymentBean.getPayment().setAmount(booking.getActivity().getAmount());
		}
	}

	public void initialize(AjaxBehaviorEvent event) {
		UIInput input = (UIInput)event.getSource();
		Object value = input.getValue();
		if (value == null) {
			initialize((Long)null);
		} else if (value instanceof Payment) {
			initialize(((Payment)value).getId());
		} else {
			initialize(Long.valueOf(""+value));
		}
	}
	
	public void handle(SelectEvent event) {
		log.debug("handle: " + event);
		Object object = event.getObject();
		if (object instanceof BookingDao) {
			forBooking(((BookingDao)object).getId());
		} else if (object instanceof PaymentDao) {
			initialize(((PaymentDao)object).getId());
		}
	}

	public void selectPaymentPayer(SelectEvent event) {
		log.debug("selectPaymentPayer: " + event);
		Object val = event.getObject();
		if (!(val instanceof Member)) {
			paymentBean.getPayment().setPayer(null);
		} else {
			Member payer = findMemberById(Long.valueOf(((Member)val).getId()));
			paymentBean.getPayment().setPayer(payer);
		}
	}

	public Payment getPayment() {
		return paymentBean.getPayment();
	}
	
	public PaymentBean getPaymentBean() {
		return paymentBean;
	}
	
	public List<BookingDao> getBookings() {
		return bookings;
	}
	
	public List<PaymentDao> getRelatedPayments() {
		return relatedPayments;
	}
	
	@Override
	public boolean isUpdateAllowed() {
		return isKassier() || isGruppe() || isVorstand() || isAdmin();
	}

	public boolean isPaymentPopupVisible() {
		return paymentPopupVisible;
	}
	
	public void setPaymentPopupVisible(boolean paymentPopupVisible) {
		this.paymentPopupVisible = paymentPopupVisible;
	}
	
}
