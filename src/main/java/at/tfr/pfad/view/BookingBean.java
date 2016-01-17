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
import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import at.tfr.pfad.BookingStatus;
import at.tfr.pfad.PaymentType;
import at.tfr.pfad.dao.ActivityRepository;
import at.tfr.pfad.dao.BookingRepository;
import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Booking_;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Squad;

/**
 * Backing bean for Booking entities.
 * <p/>
 * This class provides CRUD functionality for all Booking entities. It
 * focuses purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt>
 * for state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
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
	private BookingRepository bookingRepo;
	@Inject
	private SquadRepository squadRepo;
	@Inject
	private MemberRepository memberRepo;
	@Inject
	private ActivityRepository activityRepo;

	private Long id;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	private Booking booking;

	public Booking getBooking() {
		return this.booking;
	}

	public void setBooking(Booking Booking) {
		this.booking = Booking;
	}

	public String create() {

		this.conversation.begin();
		this.conversation.setTimeout(1800000L);
		return "create?faces-redirect=true";
	}

	public void retrieve() {

		if (FacesContext.getCurrentInstance().isPostback()) {
			return;
		}

		if (this.conversation.isTransient()) {
			this.conversation.begin();
			this.conversation.setTimeout(1800000L);
		}

		if (this.id == null) {
			this.booking = this.example;
		} else {
			this.booking = findById(getId());
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
		return isAdmin() || isGruppe();
	}

	public String update() {
		this.conversation.end();

		if (!isUpdateAllowed())
			throw new SecurityException("only admins, gruppe may update entry");

		try {
			
			if (booking.getMember().getBookings().stream().anyMatch(b-> booking.getActivity().equals(b.getActivity()))) {
				throw new Exception("Duplicate Booking: "+booking);
			}
			
			if (this.id == null) {
				this.entityManager.persist(this.booking);
				return "search?faces-redirect=true";
			} else {
				this.entityManager.merge(this.booking);
				return "view?faces-redirect=true&id=" + this.booking.getId();
			}
		} catch (Exception e) {
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
				throw new Exception("Payments exists for Booking: "+deletableEntity.getPayments());
			}

			this.entityManager.remove(deletableEntity);
			this.entityManager.flush();
			return "search?faces-redirect=true";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
			return null;
		}
	}

	/*
	 * Support searching Booking entities with pagination
	 */

	private int page;
	private long count;
	private List<Booking> pageItems;

	private Booking example = new Booking();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public Booking getExample() {
		return this.example;
	}

	public void setExample(Booking example) {
		this.example = example;
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

		if (example.getActivity() != null) {
			predicatesList.add(builder.equal(root.get(Booking_.activity), example.getActivity()));
		}
		
		if (example.getMember() != null) {
			predicatesList.add(builder.equal(root.get(Booking_.member), example.getMember()));
		}
		
		if (example.getStatus() != null) {
			predicatesList.add(builder.equal(root.get(Booking_.status), example.getStatus()));
		}
		String name = this.example.getComment();
		if (StringUtils.isNoneBlank(name)) {
			predicatesList.add(builder.like(builder.lower(root.get(Booking_.comment)), '%' + name.toLowerCase() + '%'));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Booking> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Booking entities (e.g. from inside
	 * an HtmlSelectOneMenu)
	 */

	public List<Booking> getAll() {

		CriteriaQuery<Booking> criteria = this.entityManager.getCriteriaBuilder()
				.createQuery(Booking.class);
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

				if (value == null) {
					return "";
				}

				return String.valueOf(((Booking) value).getId());
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

	public String createBookings() {
		
		int created = 0;
		conversation.end();
		
		try {
			for (Squad squad : squads) {
				for (Member scout : squad.getScouts()) {
					if (!scout.getBookings().stream().filter(b -> activity.equals(b.getActivity())).findAny().isPresent()) {
						createBooking(scout);
						created++;
					}
				}
				if (withAssistants) {
					for (Member ass : squad.getAssistants()) {
						if (!ass.getBookings().stream().filter(b -> activity.equals(b.getActivity())).findAny().isPresent()) {
							createBooking(ass);
							created++;
						}
					}
				}
			}
		} catch (Exception e) {
			log.info("createBookings: "+e, e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.toString()));
			return "";
		}
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Buchungen hergestellt: "+created, ""));

		return FacesContext.getCurrentInstance().getViewRoot().getViewId()+"?faces-redirect=true";
	}

	public String createBookingsForAllActive() {
		
		int created = 0;
		conversation.end();
		
		try {
			for (Member scout : memberRepo.findActive()) {
				if (!scout.getBookings().stream().filter(b -> activity.equals(b.getActivity())).findAny().isPresent()) {
					createBooking(scout);
					created++;
				}
			}
		} catch (Exception e) {
			log.info("createBookings: "+e, e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.toString()));
			return "";
		}
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Buchungen hergestellt: "+created, ""));

		return FacesContext.getCurrentInstance().getViewRoot().getViewId()+"?faces-redirect=true";
	}

	private void createBooking(Member scout) {
		Booking booking = new Booking();
		booking.setActivity(activity);
		booking.setMember(scout);
		booking.setStatus(BookingStatus.created);
		entityManager.persist(booking);
		entityManager.flush();
	}

	public boolean isCreateAllAllowed() {
		return isAdmin() || isGruppe();
	}

}
