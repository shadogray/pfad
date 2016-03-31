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
import java.util.stream.Collectors;

import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.richfaces.component.UISelect;

import at.tfr.pfad.ScoutRole;
import at.tfr.pfad.Sex;
import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Configuration;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Member_;
import at.tfr.pfad.model.Squad;
import at.tfr.pfad.view.ViewUtils.Month;

/**
 * Backing bean for Member entities.
 * This class provides CRUD functionality for all Member entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ViewScoped
public class MemberBean extends BaseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SquadRepository squadRepo;
	@Inject
	private MemberRepository memberRepo;
	private Boolean exampleActive;
	private Boolean exampleFree;

	/*
	 * support creating and retrieving Member entities
	 */

	public List<Squad> getSquads() {
		if (member != null && member.getGeschlecht() != null) {
			return member.getGeschlecht() == Sex.W ? squadRepo.findByLeaderFemaleEqual(member)
					: squadRepo.findByLeaderMaleEqual(member);
		}
		return new ArrayList<Squad>();
	}

	public String create() {
		return "create?faces-redirect=true";
	}

	public void retrieve() {

		if (FacesContext.getCurrentInstance().isPostback()) {
			return;
		}

		if (this.id == null) {
			this.member = memberExample;
		} else {
			if (this.member == null || !this.member.getId().equals(id)) {
				this.member = findById(getId());
				if (this.member.getTrupp() != null) this.member.getTrupp().getId();
				this.member.getSiblings().size();
				this.member.getFunktionen().size();
				if (this.member.getVollzahler() != null) {
					this.member.getVollzahler().getId();
					filteredMembers.add(member.getVollzahler());
				}
			}
		}
	}

	public Member findById(Long id) {

		return this.entityManager.find(Member.class, id);
	}

	/*
	 * support updating and deleting Member entities
	 */

	@Transactional
	public String update() {

		if (!isUpdateAllowed())
			throw new SecurityException("Update denied for: "+sessionBean.getUser());
		
		log.info("updated " + member + " by " + sessionContext.getCallerPrincipal());

		try {
			if (this.id == null) {
				this.entityManager.persist(this.member);
				if (StringUtils.isEmpty(member.getBVKey())) {
					member.setBVKey(Configuration.BADEN_KEYPFX + member.getId());
				}
				if (this.member.getId() != null) {
					return "view?faces-redirect=true&id=" + this.member.getId();
				}
				return "search?faces-redirect=true";
			} else {
				if (StringUtils.isEmpty(member.getBVKey())) {
					member.setBVKey(Configuration.BADEN_KEYPFX + member.getId());
				}
				member = entityManager.merge(member);
				return "view?faces-redirect=true&id=" + this.member.getId();
			}
		} catch (Exception e) {
			log.info("update: "+e, e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
			return null;
		}
	}

	public boolean isUpdateAllowed() {
		return isAdmin() || isGruppe() || isVorstand() || (isLeiter() && !isRegistrationEnd());
	}

	@Transactional
	public String delete() {

		if (!isDeleteAllowed())
			throw new SecurityException("only admins may delete entry");

		log.info("deleted " + member + " by " + sessionContext.getCallerPrincipal());

		try {
			Member deletableEntity = findById(getId());

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
	 * support searching Member entities with pagination
	 */

	private List<Member> pageItems;

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public Member getExample() {
		return memberExample;
	}

	public void setExample(Member example) {
		memberExample = example;
	}

	public String search() {
		this.page = 0;
		return "";
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<Member> root = countCriteria.from(Member.class);
		countCriteria = countCriteria.select(builder.count(root)).where(getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria).getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Member> criteria = builder.createQuery(Member.class);
		root = criteria.from(Member.class);
		root.fetch(Member_.trupp, JoinType.LEFT);
		TypedQuery<Member> query = this.entityManager.createQuery(
				criteria.select(root).where(getSearchPredicates(root)).distinct(true).orderBy(builder.asc(root.get(Member_.name)),
						builder.asc(root.get(Member_.vorname)), builder.asc(root.get(Member_.id))));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Member> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		String BVKey = memberExample.getBVKey();
		if (BVKey != null && !"".equals(BVKey)) {
			predicatesList
					.add(builder.like(builder.lower(root.get(Member_.bvKey)), '%' + BVKey.toLowerCase() + '%'));
		}
		long PersonenKey = memberExample.getPersonenKey();
		if (PersonenKey != 0) {
			predicatesList.add(builder.equal(root.get(Member_.personenKey), PersonenKey));
		}
		String Titel = memberExample.getTitel();
		if (Titel != null && !"".equals(Titel)) {
			predicatesList
					.add(builder.like(builder.lower(root.get(Member_.titel)), '%' + Titel.toLowerCase() + '%'));
		}
		String Name = memberExample.getName();
		if (Name != null && !"".equals(Name)) {
			predicatesList.add(builder.like(builder.lower(root.get(Member_.name)), '%' + Name.toLowerCase() + '%'));
		}

		String Vorname = memberExample.getVorname();
		if (Vorname != null && !"".equals(Vorname)) {
			predicatesList
					.add(builder.like(builder.lower(root.get(Member_.vorname)), '%' + Vorname.toLowerCase() + '%'));
		}
		String Telefon = memberExample.getTelefon();
		if (Telefon != null && !"".equals(Telefon)) {
			predicatesList
					.add(builder.like(builder.lower(root.get(Member_.telefon)), '%' + Telefon.toLowerCase() + '%'));
		}
		if (exampleActive != null) {
			predicatesList.add(builder.equal(root.get(Member_.aktiv), exampleActive));
		}
		if (exampleFree != null) {
			predicatesList.add(builder.equal(root.get(Member_.free), exampleFree));
		}

		Squad trupp = memberExample.getTrupp();
		if (trupp != null) {
			predicatesList.add(builder.equal(root.get(Member_.trupp), trupp));
		}

		if (memberExample.getFunktionen() != null && !memberExample.getFunktionen().isEmpty()
				&& memberExample.getFunktionen().iterator().next() != null) {
			predicatesList.add(root.join(Member_.funktionen).in(memberExample.getFunktionen()));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Member> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	public Boolean getExampleActive() {
		return exampleActive;
	}
	
	public void setExampleActive(Boolean exampleActive) {
		this.exampleActive = exampleActive;
	}
	
	public Boolean getExampleFree() {
		return exampleFree;
	}
	
	public void setExampleFree(Boolean exampleFree) {
		this.exampleFree = exampleFree;
	}
	
	/*
	 * support listing and POSTing back Member entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<Member> getAll() {

		CriteriaQuery<Member> criteria = this.entityManager.getCriteriaBuilder().createQuery(Member.class);
		return this.entityManager.createQuery(criteria.select(criteria.from(Member.class))).getResultList().stream()
				.sorted().collect(Collectors.toList());
	}

	public List<Member> getSelectableLeaders() {

		CriteriaQuery<Member> criteria = this.entityManager.getCriteriaBuilder().createQuery(Member.class);
		return this.entityManager.createQuery(criteria.select(criteria.from(Member.class))).getResultList().stream()
				.filter(m -> m.isAktiv()).sorted().collect(Collectors.toList());
	}

	public List<Member> getActive() {

		CriteriaQuery<Member> criteria = this.entityManager.getCriteriaBuilder().createQuery(Member.class);
		List<Member> resultList = this.entityManager.createQuery(criteria.select(criteria.from(Member.class))).getResultList();
		if (isAdmin()) {
			return resultList;
		}
		return resultList.stream()
				.filter(m -> m.isAktiv() || m.isAktivExtern()).sorted().collect(Collectors.toList());
	}

	public List<Member> getActiveNoVollzahler() {

		CriteriaQuery<Member> criteria = this.entityManager.getCriteriaBuilder().createQuery(Member.class);
		List<Member> resultList = this.entityManager.createQuery(criteria.select(criteria.from(Member.class))).getResultList();
		if (isAdmin()) {
			return resultList;
		}
		return resultList.stream()
				.filter(m -> (m.isAktiv() || m.isAktivExtern()) && m.getVollzahler() == null).sorted().collect(Collectors.toList());
	}

	public Converter getConverter() {

		final MemberBean ejbProxy = this.sessionContext.getBusinessObject(MemberBean.class);

		return new Converter() {

			@Override
			public Object getAsObject(FacesContext context, UIComponent component, String value) {
				return ejbProxy.findById(Long.valueOf(value));
			}

			@Override
			public String getAsString(FacesContext context, UIComponent component, Object value) {
				if (value instanceof Member) 
					return ""+((Member)value).getId();
				return ""+(value != null ? value : "");
			}
		};
	}

	/*
	 * support adding children to bidirectional, one-to-many tables
	 */

	private Member add = new Member();

	public Member getAdd() {
		return this.add;
	}

	public Member getAdded() {
		Member added = this.add;
		this.add = new Member();
		return added;
	}

	public List<String> getDistinctName() {
		return memberRepo.findDistinctName();
	}

	public List<String> getDistinctVorname() {
		return memberRepo.findDistinctVorname();
	}

	public List<String> getDistinctPLZ() {
		return memberRepo.findDistinctPLZ();
	}

	public List<String> getDistinctOrt() {
		return memberRepo.findDistinctOrt();
	}

	public List<String> getDistinctStrasse() {
		return memberRepo.findDistinctStrasse();
	}

	public List<String> getDistinctTitel() {
		return memberRepo.findDistinctTitel();
	}

	public List<String> getDistinctAnrede() {
		return memberRepo.findDistinctAnrede();
	}

	public List<String> getDistinctReligion() {
		return memberRepo.findDistinctReligion();
	}
	
	public String logout() {
		try {
			((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest()).logout();
		} catch (Exception e) {}
		return "index?faces-redirect=true";
	}
	
	public List<ScoutRole> getScoutRoles() {
		return Arrays.asList(ScoutRole.values());
	}
	
	public List<Sex> getSexes() {
		return Arrays.asList(Sex.values());
	}

	public List<Month> getMonths() {
		return Arrays.asList(Month.values());
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
}
