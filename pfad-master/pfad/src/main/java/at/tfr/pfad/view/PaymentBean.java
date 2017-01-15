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
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.richfaces.component.UISelect;
import org.richfaces.model.CollectionDataModel;

import at.tfr.pfad.PaymentType;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Booking_;
import at.tfr.pfad.model.Payment;
import at.tfr.pfad.model.Payment_;

/**
 * Backing bean for Payment entities. This class provides CRUD functionality for
 * all Payment entities. It focuses purely on Java EE 6 standards (e.g.
 * <tt>&#64;ConversationScoped</tt> for state management,
 * <tt>PersistenceContext</tt> for persistence, <tt>CriteriaBuilder</tt> for
 * searches) rather than introducing a CRUD framework or custom base class.
 */

@Named
@Stateful
@ViewScoped
public class PaymentBean extends BaseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Payment entities
	 */

	private Float amountFrom;
	private Float amountTo;

	public String create() {
		return "create?faces-redirect=true";
	}

	public void retrieve() {

		try {
			
			if (!isViewAllowed()) {
				throw new SecurityException("View prohibit for: "+sessionBean.getUser());
			}
			
			FacesContext ctx = FacesContext.getCurrentInstance();
			if (ctx.isPostback() && !ctx.getPartialViewContext().isAjaxRequest()) {
				return;
			}
	
			if (id == null) {
				payment = getPaymentExample();
			} else {
				payment = findById(getId());
				payment.getBookings().stream().findFirst().ifPresent(b -> payment.updateType(b));
				paymentPayer = payment.getPayer();
				if (payment.getPayer() != null) {
					filteredPayers.add(payment.getPayer());
				}
			}
		} catch (Exception e) {
			log.info("retrieve: "+e, e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
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

	enum Command {
		update, createAndNew, save
	}

	public String update(Command command) {
		
		if (!isUpdateAllowed())
			throw new SecurityException("Update disallowed for: "+sessionBean.getUser());
		
		payment.setPayer(paymentPayer);
		
		try {
			if (payment.getId() == null) {
				if (this.payment.getPaymentDate() == null) {
					this.payment.setPaymentDate(new Date());
				}
				this.entityManager.persist(this.payment);
				this.entityManager.flush();
			} else {
				payment = entityManager.merge(payment);
				this.entityManager.flush();
			}
			payment.getBookings().stream().filter(b->b.getActivity() != null).map(b->b.getActivity()).collect(Collectors.toList()); // for lazy init exc
			id = payment.getId();
			switch (command) {
			case createAndNew:
				return "create?faces-redirect=true";
			case save:
				return "view?faces-redirect=true&id=" + payment.getId();
			case update:
				return "search?faces-redirect=true";
			}
			return "search?faces-redirect=true";
		} catch (Exception e) {
			log.info("update: "+e, e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
			return null;
		}
	}

	public void deleteBooking(Long id) {
		try {
			Optional<Booking> bOpt = payment.getBookings().stream().filter(b->b.getId().equals(id)).findFirst();
			if (bOpt.isPresent()) {
				payment.getBookings().remove(bOpt.get());
				//bOpt.get().getPayments().remove(payment); Not initialized - so not necessary?!
			}
		} catch (Exception e) {
			log.info("deleteBooking: "+e, e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
		}
	}
	
	public String delete() {

		if (!isDeleteAllowed())
			throw new SecurityException("only admins may delete entry");

		try {
			Payment deletableEntity = findById(getId());

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
	 * Support searching Payment entities with pagination
	 */

	private List<PaymentUI> pageItems;
	private javax.faces.model.DataModel<PaymentUI> dataModel;

	private Boolean exampleFinished;
	private Boolean exampleAconto;
	private Date examplePaymentDateStart;
	private Date examplePaymentDateEnd;

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public Payment getExample() {
		return this.getPaymentExample();
	}

	public void setExample(Payment example) {
		setPaymentExample(example);
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
	
	public Boolean getExampleAconto() {
		return exampleAconto;
	}

	public void setExampleAconto(Boolean exampleAconto) {
		this.exampleAconto = exampleAconto;
	}
	
	public String search() {
		this.page = 0;
		return FacesContext.getCurrentInstance().getViewRoot().getViewId()+"?faces-redirect=true&includeViewParams=true";
	}

	public void paginate() {

		if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
			return;
		}
		
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
				.createQuery(criteria.select(root).distinct(true).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(getPageSize());
		this.pageItems = query.getResultList().stream().map(p -> new PaymentUI(p)).collect(Collectors.toList());
		dataModel = new CollectionDataModel<>(pageItems);
	}

	private Predicate[] getSearchPredicates(Root<Payment> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		if (examplePayer != null && examplePayer.getId() != null) {
			predicatesList.add(builder.equal(root.get(Payment_.payer), examplePayer));
		}

		if (amountFrom != null) {
			predicatesList.add(builder.greaterThanOrEqualTo(root.get(Payment_.amount), amountFrom));
		}

		if (amountTo != null) {
			predicatesList.add(builder.lessThanOrEqualTo(root.get(Payment_.amount), amountTo));
		}

		Boolean finished = this.exampleFinished;
		if (finished != null) {
			predicatesList.add(builder.equal(root.get(Payment_.finished), finished));
		}

		Boolean aconto = this.exampleAconto;
		if (aconto != null) {
			predicatesList.add(builder.equal(root.get(Payment_.aconto), aconto));
		}

		PaymentType type = this.getPaymentExample().getType();
		if (type != null) {
			predicatesList.add(builder.equal(root.get(Payment_.type), type));
		}

		if (exampleBooking != null && exampleBooking.getId() != null) {
			predicatesList.add(builder.isMember(exampleBooking, root.get(Payment_.bookings)));
		}

		if (exampleActivity != null && exampleActivity.getId() != null) {
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

	public List<PaymentUI> getPageItems() {
		return this.pageItems;
	}

	public javax.faces.model.DataModel<PaymentUI> getDataModel() {
		return dataModel;
	}
	
	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Payment entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public Float getAmountFrom() {
		return amountFrom;
	}

	public void setAmountFrom(Float amountFrom) {
		this.amountFrom = amountFrom;
	}

	public Float getAmountTo() {
		return amountTo;
	}

	public void setAmountTo(Float amountTo) {
		this.amountTo = amountTo;
	}

	public List<Payment> getAll() {

		CriteriaQuery<Payment> criteria = this.entityManager.getCriteriaBuilder().createQuery(Payment.class);
		return this.entityManager.createQuery(criteria.select(criteria.from(Payment.class))).getResultList();
	}

	public Converter getConverter() {

		final PaymentBean ejbProxy = this.sessionContext.getBusinessObject(PaymentBean.class);

		return new Converter() {

			@Override
			public Object getAsObject(FacesContext context, UIComponent component, String value) {
				if (StringUtils.isBlank(value))
					return null;
				return ejbProxy.findById(Long.valueOf(value));
			}

			@Override
			public String getAsString(FacesContext context, UIComponent component, Object value) {
				if (value instanceof Payment) 
					return ""+((Payment)value).getId();
				return ""+(value != null ? value : "");
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

	public void handle(AjaxBehaviorEvent event) {
		log.debug("handle: " + event);
		if (event != null && event.getSource() instanceof UISelect) {
			String val = (String)((UISelect) event.getSource()).getSubmittedValue();
			if (StringUtils.isNotBlank(val)) {
				setId(Long.valueOf(val));
				retrieve();
			}
		}
	}
	
	@Override
	public List<Booking> filterBookings(FacesContext facesContext, UIComponent component, final String filter) {
		if (StringUtils.isNotBlank(filter) && filter.length() < 16) {
			if (payment != null && payment.getBookings().size() > 0) {
				Booking b = payment.getBookings().iterator().next();
				filteredBookings = bookings.filtered(facesContext, component, filter, b.getActivity(), b.getMember().getStrasse());
				filteredBookings.removeAll(payment.getBookings());
			} else {
				filteredBookings = bookings.filtered(facesContext, component, filter);
			}
		}
		return filteredBookings;
	}
	
}
