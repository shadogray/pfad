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
import org.richfaces.model.CollectionDataModel;

import at.tfr.pfad.ScoutRole;
import at.tfr.pfad.Sex;
import at.tfr.pfad.dao.FunctionRepository;
import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Configuration;
import at.tfr.pfad.model.Function;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Member_;
import at.tfr.pfad.model.Squad;
import at.tfr.pfad.view.ViewUtils.Month;
import at.tfr.pfad.view.validator.MemberValidator;
import at.tfr.pfad.view.validator.ValidationResult;

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
	@Inject
	private FunctionRepository functionRepo;
	@Inject
	private MemberValidator memberValidator;
	private Boolean exampleActive;
	private Boolean exampleFree;
	private List<Function> exampleFunctions;
	private Collection<ValidationResult> validationResults = new ArrayList<>();

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
			this.member = getMemberExample();
		} else {
			if (this.member == null || !this.member.getId().equals(id)) {
				this.member = findById(getId());
				if (this.member.getTrupp() != null) this.member.getTrupp().getId();
				this.member.getSiblings().size();
				this.member.getParents().size();
				this.member.getFunktionen().size();
				if (this.member.getVollzahler() != null) {
					this.member.getVollzahler().getId();
					filteredMembers.add(member.getVollzahler());
				}
			}
			this.validationResults = validateMember(member, false);
		}
	}

	public Member findById(Long id) {
		return memberRepo.fetchBy(id);
	}

	/*
	 * support updating and deleting Member entities
	 */

	@Transactional
	public String update() {

		if (!isUpdateAllowed()) {
			throw new SecurityException("Update denied for: "+sessionBean.getUser());
		}
		
		log.info("updated " + member + " by " + sessionContext.getCallerPrincipal());

		try {
			if (id == null) {
				entityManager.persist(member);
				id = member.getId();

				if (StringUtils.isEmpty(member.getBVKey())) {
					member.setBVKey(Configuration.BADEN_KEYPFX + member.getId());
				}
				validateMember(member, true);
				
				if (member.getId() != null) {
					return "view?faces-redirect=true&id=" + member.getId();
				}
				return "search?faces-redirect=true";
			} else {
				if (StringUtils.isEmpty(member.getBVKey())) {
					member.setBVKey(Configuration.BADEN_KEYPFX + member.getId());
				}

				member = entityManager.merge(member);
				validateMember(member, true);

				return "view?faces-redirect=true&id=" + member.getId();
			}
			
		} catch (Exception e) {
			log.info("update: "+e, e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
			return null;
		}
	}

	private Collection<ValidationResult> validateMember(Member member, boolean toMessages) {
		Collection<Member> leaders = squadRepo.findLeaders();
		List<ValidationResult> vr = memberValidator.validate(member, leaders);
		if (!vr.isEmpty() && toMessages) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(vr.toString()));
		}
		return vr;
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

	public Collection<ValidationResult> getValidationResults() {
		return validationResults;
	}
	
	/*
	 * support searching Member entities with pagination
	 */

	private List<Member> pageItems;
	private javax.faces.model.DataModel<Member> dataModel;

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public Member getExample() {
		return getMemberExample();
	}

	public void setExample(Member example) {
		setMemberExample(example);
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
		dataModel = new CollectionDataModel<>(pageItems);
	}

	private Predicate[] getSearchPredicates(Root<Member> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		String BVKey = getMemberExample().getBVKey();
		if (BVKey != null && !"".equals(BVKey)) {
			predicatesList
					.add(builder.like(builder.lower(root.get(Member_.bvKey)), '%' + BVKey.toLowerCase() + '%'));
		}
		long PersonenKey = getMemberExample().getPersonenKey();
		if (PersonenKey != 0) {
			predicatesList.add(builder.equal(root.get(Member_.personenKey), PersonenKey));
		}
		String Titel = getMemberExample().getTitel();
		if (Titel != null && !"".equals(Titel)) {
			predicatesList
					.add(builder.like(builder.lower(root.get(Member_.titel)), '%' + Titel.toLowerCase() + '%'));
		}
		String Name = getMemberExample().getName();
		if (Name != null && !"".equals(Name)) {
			predicatesList.add(builder.like(builder.lower(root.get(Member_.name)), '%' + Name.toLowerCase() + '%'));
		}

		String Vorname = getMemberExample().getVorname();
		if (Vorname != null && !"".equals(Vorname)) {
			predicatesList
					.add(builder.like(builder.lower(root.get(Member_.vorname)), '%' + Vorname.toLowerCase() + '%'));
		}
		String Telefon = getMemberExample().getTelefon();
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

		Squad trupp = getMemberExample().getTrupp();
		if (trupp != null) {
			predicatesList.add(builder.equal(root.get(Member_.trupp), trupp));
		}

		if (getExampleFunctions() != null && !getExampleFunctions().isEmpty()) {
			predicatesList.add(root.join(Member_.funktionen).in(
					getExampleFunctions().stream().filter(f-> f!=null && f.getId() != null).collect(Collectors.toList())));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Member> getPageItems() {
		return pageItems;
	}
	
	public javax.faces.model.DataModel<Member> getDataModel() {
		return dataModel;
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
	
	public List<Function> getExampleFunctions() {
		return exampleFunctions;
	}
	
	public void setExampleFunctions(List<Function> exampleFunctions) {
		this.exampleFunctions = exampleFunctions;
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
				if (StringUtils.isBlank(value))
					return null;
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

	public Converter getListConverter() {
		return new Converter() {
			
			final MemberBean ejbProxy = sessionContext.getBusinessObject(MemberBean.class);
			
			@Override
			public String getAsString(FacesContext context, UIComponent component, Object value) {
				if (value instanceof Collection) {
					return ((Collection<Member>)value).stream().filter(o->o != null)
							.filter(f->f.getId() != null).map(f->f.getId().toString()).collect(Collectors.joining(","));
				}
				return "";
			}
			
			@Override
			public Object getAsObject(FacesContext context, UIComponent component, String value) {
				if (StringUtils.isNotBlank(value)) {
					return Stream.of(value.split(","))
							.map(id->ejbProxy.findById(Long.valueOf(id)))
							.filter(o->o != null).collect(Collectors.toList());
				}
				return new ArrayList<>();
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

	public List<String> getDistinctNameLike(String name) {
		return memberRepo.findDistinctNameLike(("%"+name+"%").toLowerCase());
	}

	public List<String> getDistinctVorname() {
		return memberRepo.findDistinctVorname();
	}

	public List<String> getDistinctVornameLike(String vorname) {
		return memberRepo.findDistinctVornameLike(("%"+vorname+"%").toLowerCase());
	}

	public List<String> getDistinctPLZ() {
		return memberRepo.findDistinctPLZ();
	}

	public List<String> getDistinctPLZLike(String plz) {
		return memberRepo.findDistinctPLZLike(("%"+plz+"%").toLowerCase());
	}

	public List<String> getDistinctOrt() {
		return memberRepo.findDistinctOrt();
	}

	public List<String> getDistinctOrtLike(String ort) {
		return memberRepo.findDistinctOrtLike(("%"+ort+"%").toLowerCase());
	}

	public List<String> getDistinctStrasse() {
		return memberRepo.findDistinctStrasse();
	}

	public List<String> getDistinctStrasseLike(String strasse) {
		return memberRepo.findDistinctStrasseLike(("%"+strasse+"%").toLowerCase());
	}

	public List<String> getDistinctTitel() {
		return memberRepo.findDistinctTitel();
	}

	public List<String> getDistinctTitelLike(String titel) {
		return memberRepo.findDistinctTitelLike(("%"+titel+"%").toLowerCase());
	}

	public List<String> getDistinctAnrede() {
		return memberRepo.findDistinctAnrede();
	}

	public List<String> getDistinctAnredeLike(String anrede) {
		return memberRepo.findDistinctAnredeLike(("%"+anrede+"%").toLowerCase());
	}

	public List<String> getDistinctReligion() {
		return memberRepo.findDistinctReligion();
	}
	
	public List<String> getDistinctReligionLike(String religion) {
		return memberRepo.findDistinctReligionLike(("%"+religion+"%").toLowerCase());
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
