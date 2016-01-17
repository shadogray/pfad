/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.dao;

import java.util.List;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Member_;
import at.tfr.pfad.model.Squad;
import at.tfr.pfad.model.Squad_;

@Repository
public abstract class MemberRepository implements EntityRepository<Member, Long>, CriteriaSupport<Member> {

	public List<Member> findAll() {
		return criteria().orderAsc(Member_.name).orderAsc(Member_.vorname)
				.orderAsc(Member_.gebJahr).orderAsc(Member_.gebMonat).getResultList();
	}
	
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
	

	@Query(named="distName")
	public abstract List<String> findDistinctName();

	@Query(named="distVorname")
	public abstract List<String> findDistinctVorname();

	@Query(named="distPLZ")
	public abstract List<String> findDistinctPLZ();

	@Query(named="distOrt")
	public abstract List<String> findDistinctOrt();

	@Query(named="distStrasse")
	public abstract List<String> findDistinctStrasse();

	@Query(named="distTitel")
	public abstract List<String> findDistinctTitel();

	@Query(named="distAnrede")
	public abstract List<String> findDistinctAnrede();

	@Query(named="distReligion")
	public abstract List<String> findDistinctReligion();

}
