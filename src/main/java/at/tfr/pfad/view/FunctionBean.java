/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.io.Serializable;
import java.util.ArrayList;
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

import org.apache.commons.lang3.StringUtils;

import at.tfr.pfad.model.Function;
import at.tfr.pfad.model.Function_;

/**
 * Backing bean for Function entities.
 * This class provides CRUD functionality for all Function entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class FunctionBean extends BaseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Function entities
	 */

	private Long id;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	private Function function;

	public Function getFunction() {
		return this.function;
	}

	public void setFunction(Function function) {
		this.function = function;
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
			this.function = this.example;
		} else {
			this.function = findById(getId());
		}
	}

	public Function findById(Long id) {

		return this.entityManager.find(Function.class, id);
	}

	/*
	 * Support updating and deleting Function entities
	 */

	@Override
	public boolean isUpdateAllowed() {
		return isAdmin() || isGruppe() || isVorstand();
	}

	public String update() {
		this.conversation.end();

		if (!isUpdateAllowed())
			throw new SecurityException("only admins, gruppe may update entry");

		log.info("updated " + function + " by " + sessionContext.getCallerPrincipal());

		try {
			if (this.id == null) {
				this.entityManager.persist(this.function);
				return "search?faces-redirect=true";
			} else {
				this.entityManager.merge(this.function);
				return "view?faces-redirect=true&id=" + this.function.getId();
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

		log.info("deleted " + function + " by " + sessionContext.getCallerPrincipal());

		try {
			Function deletableEntity = findById(getId());

			this.entityManager.remove(deletableEntity);
			this.entityManager.flush();
			return "search?faces-redirect=true";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
			return null;
		}
	}

	/*
	 * Support searching Function entities with pagination
	 */

	private int page;
	private long count;
	private List<Function> pageItems;

	private Function example = new Function();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public Function getExample() {
		return this.example;
	}

	public void setExample(Function example) {
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
		Root<Function> root = countCriteria.from(Function.class);
		countCriteria = countCriteria.select(builder.count(root)).where(getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria).getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Function> criteria = builder.createQuery(Function.class);
		root = criteria.from(Function.class);
		TypedQuery<Function> query = this.entityManager
				.createQuery(criteria.select(root).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Function> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		String function = this.example.getFunction();
		if (function != null && !"".equals(function)) {
			predicatesList
					.add(builder.like(builder.lower(root.get(Function_.function)), '%' + function.toLowerCase() + '%'));
		}
		String key = this.example.getKey();
		if (key != null && !"".equals(key)) {
			predicatesList.add(builder.like(builder.lower(root.get(Function_.key)), '%' + key.toLowerCase() + '%'));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Function> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Function entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<Function> getAll() {

		CriteriaQuery<Function> criteria = this.entityManager.getCriteriaBuilder().createQuery(Function.class);
		return this.entityManager.createQuery(criteria.select(criteria.from(Function.class))).getResultList();
	}

	public Converter getConverter() {

		final FunctionBean ejbProxy = this.sessionContext.getBusinessObject(FunctionBean.class);

		return new Converter() {

			@Override
			public Object getAsObject(FacesContext context, UIComponent component, String value) {

				if (StringUtils.isBlank(value))
					return null;

				return ejbProxy.findById(Long.valueOf(value));
			}

			@Override
			public String getAsString(FacesContext context, UIComponent component, Object value) {

				if (value == null) {
					return "";
				}

				return String.valueOf(((Function) value).getId());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Function add = new Function();

	public Function getAdd() {
		return this.add;
	}

	public Function getAdded() {
		Function added = this.add;
		this.add = new Function();
		return added;
	}
}
