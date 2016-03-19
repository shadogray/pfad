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
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import at.tfr.pfad.ActivityStatus;
import at.tfr.pfad.ActivityType;
import at.tfr.pfad.dao.ActivityRepository;
import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Activity_;

/**
 * Backing bean for Activity entities.
 * This class provides CRUD functionality for all Activity entities. It
 * focuses purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt>
 * for state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ViewScoped
public class ActivityBean extends BaseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Activity entities
	 */

	@Inject
	private ActivityRepository activityRepo;
	
	private Long id;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	private Activity activity;

	public Activity getActivity() {
		return this.activity;
	}

	public void setActivity(Activity Activity) {
		this.activity = Activity;
	}

	public String create() {
		return "create?faces-redirect=true";
	}

	public void retrieve() {

		if (FacesContext.getCurrentInstance().isPostback()) {
			return;
		}

		if (this.id == null) {
			this.activity = this.example;
		} else {
			this.activity = findById(getId());
		}
	}

	public Activity findById(Long id) {

		return this.entityManager.find(Activity.class, id);
	}

	/*
	 * Support updating and deleting Activity entities
	 */

	@Override
	public boolean isUpdateAllowed() {
		return isAdmin() || isGruppe() || isVorstand();
	}

	public String update() {

		if (!isUpdateAllowed())
			throw new SecurityException("only admins, gruppe may update entry");

		try {
			if (this.id == null) {
				this.entityManager.persist(this.activity);
				return "search?faces-redirect=true";
			} else {
				this.entityManager.merge(this.activity);
				return "view?faces-redirect=true&id=" + this.activity.getId();
			}
		} catch (Exception e) {
			log.info("update: "+e, e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
			return null;
		}
	}

	public String delete() {

		if (!isDeleteAllowed())
			throw new SecurityException("only admins may delete entry");

		try {
			Activity deletableEntity = findById(getId());

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
	 * Support searching Activity entities with pagination
	 */

	private List<Activity> pageItems;

	private Activity example = new Activity();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public Activity getExample() {
		return this.example;
	}

	public void setExample(Activity example) {
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
		Root<Activity> root = countCriteria.from(Activity.class);
		countCriteria = countCriteria.select(builder.count(root)).where(getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria).getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Activity> criteria = builder.createQuery(Activity.class);
		root = criteria.from(Activity.class);
		TypedQuery<Activity> query = this.entityManager
				.createQuery(criteria.select(root).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Activity> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		String name = this.example.getName();
		if (StringUtils.isNoneBlank(name)) {
			predicatesList.add(builder.like(builder.lower(root.get(Activity_.name)), '%' + name.toLowerCase() + '%'));
		}
		
		if (example.getType() != null) {
			predicatesList.add(builder.equal(root.get(Activity_.type), example.getType()));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Activity> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Activity entities (e.g. from inside
	 * an HtmlSelectOneMenu)
	 */

	public List<Activity> getAll() {

		CriteriaQuery<Activity> criteria = this.entityManager.getCriteriaBuilder()
				.createQuery(Activity.class);
		return this.entityManager.createQuery(criteria.select(criteria.from(Activity.class))).getResultList();
	}
	
	public List<Activity> getActive() {
		return activityRepo.findActive();
	}

	public Converter getConverter() {

		final ActivityBean ejbProxy = this.sessionContext.getBusinessObject(ActivityBean.class);

		return new Converter() {

			@Override
			public Object getAsObject(FacesContext context, UIComponent component, String value) {
				return ejbProxy.findById(Long.valueOf(value));
			}

			@Override
			public String getAsString(FacesContext context, UIComponent component, Object value) {
				if (value instanceof Activity) 
					return ""+((Activity)value).getId();
				return ""+(value != null ? value : "");
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Activity add = new Activity();

	public Activity getAdd() {
		return this.add;
	}

	public Activity getAdded() {
		Activity added = this.add;
		this.add = new Activity();
		return added;
	}
	
	public List<ActivityType> getTypes() {
		return Arrays.asList(ActivityType.values());
	}

	public List<ActivityStatus> getStati() {
		return Arrays.asList(ActivityStatus.values());
	}

	public List<Role> getRoles() {
		return Arrays.asList(Role.values());
	}
}
