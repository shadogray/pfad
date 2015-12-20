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
import java.util.stream.Collectors;

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

import at.tfr.pfad.SquadType;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Squad;

/**
 * Backing bean for Squad entities.
 * <p/>
 * This class provides CRUD functionality for all Squad entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class SquadBean extends BaseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Squad entities
	 */

	private Long id;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	private Squad squad;

	public Squad getSquad() {
		return this.squad;
	}

	public void setSquad(Squad squad) {
		this.squad = squad;
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
			this.squad = this.example;
		} else {
			this.squad = findById(getId());
		}
	}

	public Squad findById(Long id) {

		return this.entityManager.find(Squad.class, id);
	}

	/*
	 * Support updating and deleting Squad entities
	 */

	@Override
	public boolean isUpdateAllowed() {
		return isUpdateAllowed(squad);
	}

	public boolean isUpdateAllowed(Squad squad) {
		return isAdmin() || isGruppe() || 
				(sessionContext.getCallerPrincipal().getName().equals(squad.getLogin()) && !isRegistrationEnd());
	}

	public String update() {
		this.conversation.end();

		if (!isUpdateAllowed())
			throw new SecurityException("only admins, gruppe, "+squad.getLogin()+" may update entry");
		
		log.info("updated "+squad+" by "+sessionContext.getCallerPrincipal());
		this.squad.setChanged(new Date());
		this.squad.setChangedBy(sessionContext.getCallerPrincipal().getName());

		try {
			if (this.id == null) {
				this.squad.setCreated(new Date());
				this.squad.setCreatedBy(sessionContext.getCallerPrincipal().getName());
				this.entityManager.persist(this.squad);
				if (this.squad.getId() != null) {
					return "view?faces-redirect=true&id=" + this.squad.getId();
				}
				return "search?faces-redirect=true";
			} else {
				this.entityManager.merge(this.squad);
				return "view?faces-redirect=true&id=" + this.squad.getId();
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	public String delete() {
		this.conversation.end();

		if (!isAdmin())
			throw new SecurityException("only admins may delete entry");
		
		log.info("deleted "+squad+" by "+sessionContext.getCallerPrincipal());

		try {
			Squad deletableEntity = findById(getId());

			this.entityManager.remove(deletableEntity);
			this.entityManager.flush();
			return "search?faces-redirect=true";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	/*
	 * Support searching Squad entities with pagination
	 */

	private int page;
	private long count;
	private List<Squad> pageItems;

	private Squad example = new Squad();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public Squad getExample() {
		return this.example;
	}

	public void setExample(Squad example) {
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
		Root<Squad> root = countCriteria.from(Squad.class);
		countCriteria = countCriteria.select(builder.count(root)).where(
				getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria)
				.getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Squad> criteria = builder.createQuery(Squad.class);
		root = criteria.from(Squad.class);
		TypedQuery<Squad> query = this.entityManager.createQuery(criteria
				.select(root).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(
				getPageSize());
		this.pageItems = query.getResultList().stream().sorted().collect(Collectors.toList());
	}

	private Predicate[] getSearchPredicates(Root<Squad> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		SquadType type = this.example.getType();
		if (type != null) {
			predicatesList.add(builder.equal(root.get("type"), type));
		}
		String name = this.example.getName();
		if (name != null && !"".equals(name)) {
			predicatesList.add(builder.like(
					builder.lower(root.<String> get("name")),
					'%' + name.toLowerCase() + '%'));
		}
		Member leaderMale = this.example.getLeaderMale();
		if (leaderMale != null) {
			predicatesList.add(builder.equal(root.get("leaderMale"), leaderMale));
		}
		Member leaderFemale = this.example.getLeaderFemale();
		if (leaderFemale != null) {
			predicatesList.add(builder.equal(root.get("leaderFemale"), leaderFemale));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Squad> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Squad entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<Squad> getAll() {

		CriteriaQuery<Squad> criteria = this.entityManager.getCriteriaBuilder()
				.createQuery(Squad.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(Squad.class))).getResultList()
				.stream().sorted().collect(Collectors.toList());
	}
	
	public Converter getConverter() {

		final SquadBean ejbProxy = this.sessionContext
				.getBusinessObject(SquadBean.class);

		return new Converter() {

			@Override
			public Object getAsObject(FacesContext context,
					UIComponent component, String value) {

				return ejbProxy.findById(Long.valueOf(value));
			}

			@Override
			public String getAsString(FacesContext context,
					UIComponent component, Object value) {

				if (value == null) {
					return "";
				}

				return String.valueOf(((Squad) value).getId());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Squad add = new Squad();

	public Squad getAdd() {
		return this.add;
	}

	public Squad getAdded() {
		Squad added = this.add;
		this.add = new Squad();
		return added;
	}
	
	public List<SquadType> getTypes() {
		return Arrays.asList(SquadType.values());
	}
}
