/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.Stateful;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.richfaces.component.UISelect;

import at.tfr.pfad.BookingStatus;
import at.tfr.pfad.PaymentType;
import at.tfr.pfad.dao.ActivityRepository;
import at.tfr.pfad.dao.BookingRepository;
import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Activity_;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Booking_;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Member_;
import at.tfr.pfad.model.Payment;
import at.tfr.pfad.model.Squad;

/**
 * Backing bean for Booking entities. This class provides CRUD functionality for
 * all Booking entities. It focuses purely on Java EE 6 standards (e.g.
 * <tt>&#64;ConversationScoped</tt> for state management,
 * <tt>PersistenceContext</tt> for persistence, <tt>CriteriaBuilder</tt> for
 * searches) rather than introducing a CRUD framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class BookingBean extends BaseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Booking entities
	 */

	@Inject
	private ActivityRepository activityRepo;
	@Inject
	private BookingActionBean bookingActionBean;
	@Inject
	private PaymentBean paymentBean;

	private boolean showFinished;
	private boolean squadBookingVisible;
	private boolean allBookingVisible;
	private boolean fromToVisible;
	private Activity sourceActivity;
	private Activity targetActivity;

	public boolean isSquadBookingVisible() {
		return squadBookingVisible;
	}

	public void setSquadBookingVisible(boolean squadBookingVisible) {
		this.squadBookingVisible = squadBookingVisible;
	}

	public boolean isAllBookingVisible() {
		return allBookingVisible;
	}

	public void setAllBookingVisible(boolean allBookingVisible) {
		this.allBookingVisible = allBookingVisible;
	}

	public boolean isShowFinished() {
		return showFinished;
	}

	public void setShowFinished(boolean showFinished) {
		this.showFinished = showFinished;
	}

	public List<Activity> getActivities() {
		if (showFinished) {
			return activityRepo.findAll();
		} else {
			return activityRepo.findActive();
		}
	}

	public Activity getSourceActivity() {
		return sourceActivity;
	}

	public void setSourceActivity(Activity sourceActivity) {
		this.sourceActivity = sourceActivity;
	}

	public Activity getTargetActivity() {
		return targetActivity;
	}

	public void setTargetActivity(Activity targetActivity) {
		this.targetActivity = targetActivity;
	}

	public boolean isFromToVisible() {
		return fromToVisible;
	}

	public void setFromToVisible(boolean fromToVisible) {
		this.fromToVisible = fromToVisible;
	}

	public String create() {

		this.conversation.begin();
		this.conversation.setTimeout(1800000L);
		return "create?faces-redirect=true";
	}

	public void retrieve() {

		FacesContext ctx = FacesContext.getCurrentInstance();
		if (ctx.isPostback() && !ctx.getPartialViewContext().isAjaxRequest()) {
			return;
		}

		if (this.conversation.isTransient()) {
			this.conversation.begin();
			this.conversation.setTimeout(1800000L);
		}

		if (this.id == null) {
			this.booking = this.bookingExample;
		} else {
			if (this.booking == null || !this.booking.getId().equals(id)) {
				this.booking = findById(getId());
			}
		}
	}

	public Booking findById(Long id) {

		return this.entityManager.find(Booking.class, id);
	}

	/*
	 * Support updating and deleting Booking entities
	 */

	@Override
	public boolean isUpdateAllowed() {
		return isAdmin() || isGruppe() || isVorstand();
	}

	public String update() {
		this.conversation.end();

		if (!isUpdateAllowed())
			throw new SecurityException("only admins, gruppe may update entry");

		try {

			if (booking.getId() == null && booking.getMember() != null && 
					entityManager.find(Member.class, booking.getMember().getId())
					.getBookings().stream().anyMatch(b -> booking.getActivity().equals(b.getActivity()))) {
				throw new Exception("Duplicate Booking: " + booking);
			}

			if (booking.getMember().getTrupp() != null) {
				booking.setSquad(booking.getMember().getTrupp());
			}

			if (this.id == null) {
				this.entityManager.persist(this.booking);
				return "search?faces-redirect=true";
			} else {
				this.entityManager.merge(this.booking);
				return "view?faces-redirect=true&id=" + this.booking.getId();
			}
		} catch (Exception e) {
			log.info("update: "+e, e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
			return null;
		}
	}

	public String delete() {
		this.conversation.end();

		if (!isDeleteAllowed())
			throw new SecurityException("only admins may delete entry");

		try {
			Booking deletableEntity = findById(getId());

			if (!deletableEntity.getPayments().isEmpty()) {
				throw new Exception("Payments exists for Booking: " + deletableEntity.getPayments());
			}

			this.entityManager.remove(deletableEntity);
			this.entityManager.flush();
			return "search?faces-redirect=true";
		} catch (Exception e) {
			log.info("update: "+e, e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
			return null;
		}
	}

	/*
	 * Support searching Booking entities with pagination
	 */

	private List<Booking> pageItems;

	public Booking getExample() {
		return this.bookingExample;
	}

	public void setExample(Booking example) {
		this.bookingExample = example;
	}

	public String search() {
		this.page = 0;
		return null;
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<Booking> root = countCriteria.from(Booking.class);
		countCriteria = countCriteria.select(builder.count(root)).where(getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria).getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Booking> criteria = builder.createQuery(Booking.class);
		root = criteria.from(Booking.class);
		TypedQuery<Booking> query = this.entityManager
				.createQuery(criteria.select(root).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Booking> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		if (bookingExample.getActivity() != null) {
			predicatesList.add(builder.equal(root.get(Booking_.activity), bookingExample.getActivity()));
		}

		Member m = bookingExample.getMember();
		if (m != null && m.getId() != null) {
			predicatesList.add(builder.equal(root.get(Booking_.member), m));
		}

		if (bookingExample.getSquad() != null) {
			predicatesList.add(builder.equal(root.get(Booking_.squad), bookingExample.getSquad()));
		}

		if (bookingExample.getStatus() != null) {
			predicatesList.add(builder.equal(root.get(Booking_.status), bookingExample.getStatus()));
		}
		String name = this.bookingExample.getComment();
		if (StringUtils.isNoneBlank(name)) {
			predicatesList.add(builder.like(builder.lower(root.get(Booking_.comment)), '%' + name.toLowerCase() + '%'));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Booking> getPageItems() {
		return this.pageItems;
	}

	/*
	 * Support listing and POSTing back Booking entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<Booking> getAll() {

		CriteriaQuery<Booking> criteria = this.entityManager.getCriteriaBuilder().createQuery(Booking.class);
		return this.entityManager.createQuery(criteria.select(criteria.from(Booking.class))).getResultList();
	}

	public Converter getConverter() {

		final BookingBean ejbProxy = this.sessionContext.getBusinessObject(BookingBean.class);

		return new Converter() {

			@Override
			public Object getAsObject(FacesContext context, UIComponent component, String value) {

				return ejbProxy.findById(Long.valueOf(value));
			}

			@Override
			public String getAsString(FacesContext context, UIComponent component, Object value) {
				return ""+(value != null ? value : "");
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Booking add = new Booking();

	public Booking getAdd() {
		return this.add;
	}

	public Booking getAdded() {
		Booking added = this.add;
		this.add = new Booking();
		return added;
	}

	public List<BookingStatus> getStati() {
		return Arrays.asList(BookingStatus.values());
	}

	private Activity activity;
	private List<Squad> squads = new ArrayList<>();
	private boolean withAssistants;

	public List<Squad> getSquads() {
		return squads;
	}

	public void setSquads(List<Squad> squads) {
		this.squads = squads;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public boolean isWithAssistants() {
		return withAssistants;
	}

	public void setWithAssistants(boolean withAssistants) {
		this.withAssistants = withAssistants;
	}

	public boolean isCreateAllAllowed() {
		return isAdmin() || isGruppe();
	}

	public String createBookings() {
		return bookingActionBean.createBookings(squads, activity, withAssistants);
	}

	public String createBookingsForAllActive() {
		return bookingActionBean.createBookingsForAllActive(activity);
	}

	public String createBookingsFromSource() {
		return bookingActionBean.createBookingsFromSource(sourceActivity, targetActivity);
	}

	public void retrieveAndGetPayment() {
		retrieve();
		if (!booking.getPayments().isEmpty()) {
			paymentBean.setId(booking.getPayments().iterator().next().getId());
		}
		paymentBean.retrieve();
		Payment pay = paymentBean.getPayment();
		pay.getBookings().add(booking);

		if (pay.getType() == null) {
			switch (booking.getActivity().getType()) {
			case Membership:
				pay.setType(PaymentType.Membership);
				break;
			case Camp:
				pay.setType(PaymentType.Camp);
				break;
			default:
			}
		}
	}

	public void handle(AjaxBehaviorEvent event) {
		log.debug("handle: " + event);
		if (event != null && event.getSource() instanceof UISelect) {
			String val = (String) ((UISelect) event.getSource()).getSubmittedValue();
			if (StringUtils.isNotBlank(val)) {
				setId(Long.valueOf(val));
				retrieve();
			}
		}
	}
}
