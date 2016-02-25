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
import java.util.Date;
import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Named;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import at.tfr.pfad.PaymentType;
import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Booking_;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Payment;
import at.tfr.pfad.model.Payment_;

/**
 * Backing bean for Payment entities.
 * This class provides CRUD functionality for all Payment entities. It
 * focuses purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt>
 * for state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class PaymentBean extends BaseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Payment entities
	 */

	private Long id;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	private Payment payment;

	public Payment getPayment() {
		return this.payment;
	}

	public void setPayment(Payment Payment) {
		this.payment = Payment;
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
			this.payment = this.example;
		} else {
			this.payment = findById(getId());
		}
	}

	public Payment findById(Long id) {

		return this.entityManager.find(Payment.class, id);
	}

	/*
	 * Support updating and deleting Payment entities
	 */

	@Override
	public boolean isUpdateAllowed() {
		return isAdmin() || isKassier() || isVorstand();
	}

	public boolean isViewAllowed() {
		return isAdmin() || isKassier() || isVorstand() || isGruppe();
	}

	public String update() {
		return update(Command.update);
	}
	
	public String createAndNew() {
		return update(Command.createAndNew);
	}
	
	public String save() {
		return update(Command.save);
	}
	
	enum Command { update, createAndNew, save }
	
	public String update(Command command) {
		this.conversation.end();

		if (!isUpdateAllowed())
			throw new SecurityException("only admins, gruppe may update entry");

		try {
			if (this.id == null) {
				if (this.payment.getPaymentDate() == null) {
					this.payment.setPaymentDate(new Date());
				}
				this.entityManager.persist(this.payment);
				switch (command) {
				case createAndNew: 
					return "create?faces-redirect=true";
				case save:
					return "create?faces-redirect=true&id="+payment.getId();
				} 
				return "search?faces-redirect=true";
			} else {
				this.entityManager.merge(this.payment);
				return "view?faces-redirect=true&id=" + this.payment.getId();
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
			Payment deletableEntity = findById(getId());

			this.entityManager.remove(deletableEntity);
			this.entityManager.flush();
			return "search?faces-redirect=true";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
			return null;
		}
	}

	/*
	 * Support searching Payment entities with pagination
	 */

	private int page;
	private long count;
	private List<Payment> pageItems;

	private Payment example = new Payment();
	private Booking exampleBooking;
	private Activity exampleActivity;
	private Boolean exampleFinished;
	private Date examplePaymentDateStart;
	private Date examplePaymentDateEnd;

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public Payment getExample() {
		return this.example;
	}

	public void setExample(Payment example) {
		this.example = example;
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

	public Date getExamplePaymentDateStart() {
		return examplePaymentDateStart;
	}

	public void setExamplePaymentDateStart(Date examplePaymentDateStart) {
		this.examplePaymentDateStart = examplePaymentDateStart;
	}

	public Date getExamplePaymentDateEnd() {
		return examplePaymentDateEnd;
	}

	public void setExamplePaymentDateEnd(Date examplePaymentDateEnd) {
		this.examplePaymentDateEnd = examplePaymentDateEnd;
	}
	
	public Boolean getExampleFinished() {
		return exampleFinished;
	}

	public void setExampleFinished(Boolean exampleFinished) {
		this.exampleFinished = exampleFinished;
	}

	public String search() {
		this.page = 0;
		return null;
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<Payment> root = countCriteria.from(Payment.class);
		countCriteria = countCriteria.select(builder.count(root)).where(getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria).getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Payment> criteria = builder.createQuery(Payment.class);
		root = criteria.from(Payment.class);
		TypedQuery<Payment> query = this.entityManager
				.createQuery(criteria.select(root).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Payment> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		Member payer = this.example.getPayer();
		if (payer != null) {
			predicatesList.add(builder.equal(root.get(Payment_.payer), payer));
		}

		Boolean finished = this.exampleFinished;
		if (finished != null) {
			predicatesList.add(builder.equal(root.get(Payment_.finished), finished));
		}

		PaymentType type = this.example.getType();
		if (type != null) {
			predicatesList.add(builder.equal(root.get(Payment_.type), type));
		}

		Booking booking = this.exampleBooking;
		if (booking != null) {
			predicatesList.add(builder.isMember(booking, root.get(Payment_.bookings)));
		}
		
		if (exampleActivity != null) {
			predicatesList.add(builder.equal(root.join(Payment_.bookings).get(Booking_.activity), exampleActivity));
		}

		if (examplePaymentDateStart != null) {
			predicatesList.add(builder.greaterThanOrEqualTo(root.get(Payment_.paymentDate), examplePaymentDateStart));
		}

		if (examplePaymentDateEnd != null) {
			predicatesList.add(builder.lessThanOrEqualTo(root.get(Payment_.paymentDate), examplePaymentDateEnd));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Payment> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Payment entities (e.g. from inside
	 * an HtmlSelectOneMenu)
	 */

	public List<Payment> getAll() {

		CriteriaQuery<Payment> criteria = this.entityManager.getCriteriaBuilder()
				.createQuery(Payment.class);
		return this.entityManager.createQuery(criteria.select(criteria.from(Payment.class))).getResultList();
	}

	public Converter getConverter() {

		final PaymentBean ejbProxy = this.sessionContext.getBusinessObject(PaymentBean.class);

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

				return String.valueOf(((Payment) value).getId());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Payment add = new Payment();

	public Payment getAdd() {
		return this.add;
	}

	public Payment getAdded() {
		Payment added = this.add;
		this.add = new Payment();
		return added;
	}
	
	public List<PaymentType> getTypes() {
		return Arrays.asList(PaymentType.values());
	}
}
