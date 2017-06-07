/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.dao;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.apache.deltaspike.data.api.EntityGraph;
import org.apache.deltaspike.data.api.EntityManagerDelegate;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

import at.tfr.pfad.model.Registration;
import at.tfr.pfad.model.Registration_;
import at.tfr.pfad.model.Squad;
import at.tfr.pfad.model.Squad_;

@ApplicationScoped
@Repository
public abstract class RegistrationRepository implements EntityRepository<Registration, Long>, CriteriaSupport<Registration>, EntityManagerDelegate<Registration> {

	@EntityGraph("fetchAll")
	public Registration fetchBy(Long id) {
		return findBy(id);
	}

	public List<Registration> fetchAll() {
		return fetchAll(0, Integer.MAX_VALUE);
	}
	
	public List<Registration> fetchAll(int max) {
		return fetchAll(0, max);
	}
	
	public List<Registration> fetchAll(int start, int max) {
		List<Long> ids = queryAllIdsIntern()
				.firstResult(start).maxResults(max).getResultList();
		return fetch(ids)
				.orderAsc(Registration_.name).orderAsc(Registration_.vorname)
				.orderAsc(Registration_.gebJahr).orderAsc(Registration_.gebMonat)
				.getResultList();
	}

	protected QueryResult<Registration> queryAllIntern(int start, int max) {
		return queryAllIntern()
				.firstResult(start)
				.maxResults(max);
	}
	
	@Query("select e from Registration e")
	protected abstract QueryResult<Registration> queryAllIntern();
	
	@Query("select e.id from Registration e")
	protected abstract QueryResult<Long> queryAllIdsIntern();
	
	@Query("select e from Registration e where e.id in (?1)")
	protected abstract QueryResult<Registration> fetch(Collection<Long> registrationIds);
	
	@Query("select e from Registration e")
	protected abstract QueryResult<Registration> fetchAllIntern();
	
	@Query(named="Registration.distName")
	public abstract List<String> findDistinctName();

	@Query(named="Registration.distVorname")
	public abstract List<String> findDistinctVorname();

	@Query(named="Registration.distPLZ")
	public abstract List<String> findDistinctPLZ();

	@Query(named="Registration.distOrt")
	public abstract List<String> findDistinctOrt();

	@Query(named="Registration.distStrasse")
	public abstract List<String> findDistinctStrasse();

}
