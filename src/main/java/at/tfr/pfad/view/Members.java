package at.tfr.pfad.view;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.enterprise.inject.Model;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Member_;

@Model
public class Members {

	private Logger log = Logger.getLogger(getClass());

	@Inject
	private EntityManager entityManager;

	public List<Member> filtered(FacesContext facesContext, UIComponent component, final String filter) {
		log.debug("filter: " + filter + " for: " + component.getId());
		CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Member> cq = cb.createQuery(Member.class);
		Root<Member> root = cq.from(Member.class);
		CriteriaQuery<Member> query = cq.select(root);
		List<Predicate> preds = new ArrayList<>();
		if (StringUtils.isNotBlank(filter) && filter.length() > 2) {
			Stream.of(filter.split(" ")).forEach(v -> preds.add(cb.or(predicatesFor(v, cb, root))));
			cq.where(cb.and(preds.toArray(new Predicate[preds.size()])));
		}
		return this.entityManager.createQuery(query).setMaxResults(10).getResultList();
	}

	Predicate[] predicatesFor(String value, CriteriaBuilder cb, Root<Member> root) {
		List<Predicate> list = new ArrayList<>();
		list.add(cb.like(cb.lower(root.get(Member_.name)), "%" + value + "%"));
		list.add(cb.like(cb.lower(root.get(Member_.vorname)), "%" + value + "%"));
		return list.toArray(new Predicate[list.size()]);
	}

}
