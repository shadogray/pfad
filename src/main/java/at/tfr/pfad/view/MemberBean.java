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
import java.util.stream.Collectors;

import javax.ejb.Stateful;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import at.tfr.pfad.Sex;
import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Configuration;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Member_;
import at.tfr.pfad.model.Squad;

/**
 * Backing bean for Member entities.
 * <p/>
 * This class provides CRUD functionality for all Member entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class MemberBean extends BaseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SquadRepository squadRepo;
	@Inject
	private MemberRepository memberRepo;

	/*
	 * Support creating and retrieving Member entities
	 */

	private Long id;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	private Member member;

	public Member getMember() {
		return this.member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public List<Squad> getSquads() {
		if (member != null && member.getGeschlecht() != null) {
			return member.getGeschlecht() == Sex.W ? squadRepo.findByLeaderFemaleEqual(member)
					: squadRepo.findByLeaderMaleEqual(member);
		}
		return new ArrayList<Squad>();
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
			this.member = this.example;
		} else {
			this.member = findById(getId());
		}
	}

	public Member findById(Long id) {

		return this.entityManager.find(Member.class, id);
	}

	/*
	 * Support updating and deleting Member entities
	 */

	public String update() {
		this.conversation.end();

		if (isUpdateAllowed())
			throw new SecurityException("only admin,gruppe,leiter may update entry");
		
		log.info("updated " + member + " by " + sessionContext.getCallerPrincipal());

		try {
			if (this.id == null) {
				this.entityManager.persist(this.member);
				if (StringUtils.isEmpty(member.getBVKey())) {
					member.setBVKey(Configuration.BADEN_KEYPFX + member.getId());
				}
				return "search?faces-redirect=true";
			} else {
				if (StringUtils.isEmpty(member.getBVKey())) {
					member.setBVKey(Configuration.BADEN_KEYPFX + member.getId());
				}
				this.entityManager.merge(this.member);
				return "view?faces-redirect=true&id=" + this.member.getId();
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
			return null;
		}
	}

	public boolean isUpdateAllowed() {
		return isAdmin() || isGruppe() || isLeiter();
	}

	public String delete() {
		this.conversation.end();

		if (!isDeleteAllowed())
			throw new SecurityException("only admins may delete entry");

		log.info("deleted " + member + " by " + sessionContext.getCallerPrincipal());

		try {
			Member deletableEntity = findById(getId());

			this.entityManager.remove(deletableEntity);
			this.entityManager.flush();
			return "search?faces-redirect=true";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
			return null;
		}
	}

	/*
	 * Support searching Member entities with pagination
	 */

	private int page;
	private long count;
	private List<Member> pageItems;

	private Member example = new Member();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public Member getExample() {
		return this.example;
	}

	public void setExample(Member example) {
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
		Root<Member> root = countCriteria.from(Member.class);
		countCriteria = countCriteria.select(builder.count(root)).where(getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria).getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Member> criteria = builder.createQuery(Member.class);
		root = criteria.from(Member.class);
		TypedQuery<Member> query = this.entityManager.createQuery(
				criteria.select(root).where(getSearchPredicates(root)).orderBy(builder.asc(root.get(Member_.Name)),
						builder.asc(root.get(Member_.Vorname)), builder.asc(root.get(Member_.id))));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Member> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		String BVKey = this.example.getBVKey();
		if (BVKey != null && !"".equals(BVKey)) {
			predicatesList
					.add(builder.like(builder.lower(root.<String> get("BVKey")), '%' + BVKey.toLowerCase() + '%'));
		}
		String GruppenSchluessel = this.example.getGruppenSchluessel();
		if (GruppenSchluessel != null && !"".equals(GruppenSchluessel)) {
			predicatesList.add(builder.like(builder.lower(root.<String> get("GruppenSchluessel")),
					'%' + GruppenSchluessel.toLowerCase() + '%'));
		}
		long PersonenKey = this.example.getPersonenKey();
		if (PersonenKey != 0) {
			predicatesList.add(builder.equal(root.get("PersonenKey"), PersonenKey));
		}
		String Titel = this.example.getTitel();
		if (Titel != null && !"".equals(Titel)) {
			predicatesList
					.add(builder.like(builder.lower(root.<String> get("Titel")), '%' + Titel.toLowerCase() + '%'));
		}
		String Name = this.example.getName();
		if (Name != null && !"".equals(Name)) {
			predicatesList.add(builder.like(builder.lower(root.<String> get("Name")), '%' + Name.toLowerCase() + '%'));
		}

		String Vorname = this.example.getVorname();
		if (Vorname != null && !"".equals(Vorname)) {
			predicatesList
					.add(builder.like(builder.lower(root.<String> get("Vorname")), '%' + Vorname.toLowerCase() + '%'));
		}
		String Telefon = this.example.getTelefon();
		if (Telefon != null && !"".equals(Telefon)) {
			predicatesList
					.add(builder.like(builder.lower(root.<String> get("Telefon")), '%' + Telefon.toLowerCase() + '%'));
		}

		Squad trupp = this.example.getTrupp();
		if (trupp != null) {
			predicatesList.add(builder.equal(root.get("Trupp"), trupp));
		}

		if (this.example.getFunktionen() != null && !this.example.getFunktionen().isEmpty()
				&& this.example.getFunktionen().iterator().next() != null) {
			predicatesList.add(root.join(Member_.Funktionen).in(this.example.getFunktionen()));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Member> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Member entities (e.g. from inside an
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
		return this.entityManager.createQuery(criteria.select(criteria.from(Member.class))).getResultList().stream()
				.filter(m -> m.isAktiv()).sorted().collect(Collectors.toList());
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

				if (value == null) {
					return "";
				}

				return String.valueOf(((Member) value).getId());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
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
}
