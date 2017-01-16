/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.dao;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.apache.deltaspike.data.api.EntityGraph;
import org.apache.deltaspike.data.api.EntityManagerDelegate;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Member_;
import at.tfr.pfad.model.Squad;
import at.tfr.pfad.model.Squad_;

@ApplicationScoped
@Repository
public abstract class SquadRepository implements EntityRepository<Squad, Long>, CriteriaSupport<Squad>, EntityManagerDelegate<Squad> {

	@EntityGraph(paths = {"leaderFemale","leaderMale","leaders","assistants"})
	public abstract Squad findById(Long id);
	
	public abstract List<Squad> findByName(String name);
	
	public abstract List<Squad> findByLeaderFemaleEqual(Member leader);

	public abstract List<Squad> findByLeaderMaleEqual(Member leader);

	public abstract List<Squad> findByLeaderFemaleEqualOrLeaderMaleEqual(Member female, Member male);
	
	public List<Squad> findByLeaderFemaleEqualOrLeaderMaleEqual(Member leader) {
		return findByLeaderFemaleEqualOrLeaderMaleEqual(leader, leader);
	}

	public List<Squad> findByAssistant(Member assistant) {
		return criteria().join(Squad_.assistants, where(Member.class).eq(Member_.id, assistant.getId()))
				.getResultList();
	}

	public List<Squad> findByResponsible(Member responsible) {
		List<Squad> leaders = findByLeaderFemaleEqual(responsible);
		leaders.addAll(findByLeaderMaleEqual(responsible));
		leaders.addAll(findByAssistant(responsible));
		return leaders;
	}

	public List<Member> findLeaders() {
		List<Member> leaders = findLeadersFemale();
		leaders.addAll(findLeadersMale());
		leaders.addAll(findAssistants());
		return leaders;
	}

	@Query(named="Squad.leadersFemale")
	public abstract List<Member> findLeadersFemale();

	@Query(named="Squad.leadersMale")
	public abstract List<Member> findLeadersMale();

	@Query(named="Squad.assistants")
	public abstract List<Member> findAssistants();

}
