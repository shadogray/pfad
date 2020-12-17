/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.dao;

import java.util.Collection;
import java.util.List;

import org.apache.deltaspike.data.api.EntityGraph;
import org.apache.deltaspike.data.api.EntityManagerDelegate;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

import at.tfr.pfad.model.Function;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Member_;
import at.tfr.pfad.model.Squad;
import at.tfr.pfad.model.Squad_;

@Repository
public abstract class MemberRepository implements EntityRepository<Member, Long>, CriteriaSupport<Member>, EntityManagerDelegate<Member> {

	@EntityGraph("fetchAll")
	public Member fetchBy(Long id) {
		return findBy(id);
	}

	public abstract List<Member> findByNameEqualIgnoreCaseAndVornameEqualIgnoreCaseAndStrasseEqualIgnoreCaseAndOrtEqualIgnoreCase(String name, String vorname, String strasse, String ort);
	
	public List<Member> fetchAll() {
		return fetchAll(0, Integer.MAX_VALUE);
	}
	
	public List<Member> fetchAll(int max) {
		return fetchAll(0, max);
	}
	
	public List<Member> fetchAll(int start, int max) {
		List<Long> ids = queryAllIdsIntern()
				.firstResult(start).maxResults(max).getResultList();
		return fetch(ids)
				.orderAsc(Member_.name).orderAsc(Member_.vorname)
				.orderAsc(Member_.gebJahr).orderAsc(Member_.gebMonat)
				.getResultList();
	}

	@Query(named="Member.withFunction")
	public abstract List<Member> findByFunction(Function function);
	
	protected QueryResult<Member> queryAllIntern(int start, int max) {
		return queryAllIntern()
				.firstResult(start)
				.maxResults(max);
	}
	
	@Query("select e from Member e")
	protected abstract QueryResult<Member> queryAllIntern();
	
	@Query("select e.id from Member e")
	protected abstract QueryResult<Long> queryAllIdsIntern();
	
	@EntityGraph("fetchAll")
	@Query("select e from Member e where e.id in (?1)")
	protected abstract QueryResult<Member> fetch(Collection<Long> memberIds);
	
	@EntityGraph("fetchAll")
	@Query("select e from Member e")
	protected abstract QueryResult<Member> fetchAllIntern();
	
	@EntityGraph("fetchAll")
	public List<Member> findActive() {
		return criteria().eq(Member_.aktiv, true).orderAsc(Member_.name).orderAsc(Member_.vorname)
				.orderAsc(Member_.gebJahr).orderAsc(Member_.gebMonat).getResultList();
	}

	public List<Member> findAccessible(Member member) {
		return criteria()
				.join(Member_.trupp,
						where(Squad.class).join(Squad_.leaderFemale, where(Member.class).eq(Member_.id, member.getId())))
				.getResultList();
	}
	

	@Query(named="Member.distName")
	public abstract List<String> findDistinctName();

	@Query(named="Member.distNameLike")
	public abstract List<String> findDistinctNameLike(String name);

	@Query(named="Member.distVorname")
	public abstract List<String> findDistinctVorname();

	@Query(named="Member.distVornameLike")
	public abstract List<String> findDistinctVornameLike(String vorname);

	@Query(named="Member.distPLZ")
	public abstract List<String> findDistinctPLZ();

	@Query(named="Member.distPLZLike")
	public abstract List<String> findDistinctPLZLike(String plz);

	@Query(named="Member.distOrt")
	public abstract List<String> findDistinctOrt();

	@Query(named="Member.distOrtLike")
	public abstract List<String> findDistinctOrtLike(String ort);

	@Query(named="Member.distStrasse")
	public abstract List<String> findDistinctStrasse();

	@Query(named="Member.distStrasseLike")
	public abstract List<String> findDistinctStrasseLike(String strasse);

	@Query(named="Member.distTitel")
	public abstract List<String> findDistinctTitel();

	@Query(named="Member.distTitelLike")
	public abstract List<String> findDistinctTitelLike(String titel);

	@Query(named="Member.distAnrede")
	public abstract List<String> findDistinctAnrede();

	@Query(named="Member.distAnredeLike")
	public abstract List<String> findDistinctAnredeLike(String anrede);

	@Query(named="Member.distReligion")
	public abstract List<String> findDistinctReligion();

	@Query(named="Member.distReligionLike")
	public abstract List<String> findDistinctReligionLike(String religion);

}
