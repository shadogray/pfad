/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Version;

import at.tfr.pfad.SquadType;
@Entity
public class Squad implements Serializable, Comparable<Squad> {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "squad_seq")
	@SequenceGenerator(name = "squad_seq", sequenceName = "squad_seq", allocationSize=1, initialValue=1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@Enumerated(EnumType.STRING)
	private SquadType type;

	@Column
	private String name;

	@ManyToOne
	private Member leaderMale;

	@ManyToOne
	private Member leaderFemale;

	@ManyToMany
	@OrderBy("name, vorname")
	private Set<Member> assistants = new HashSet<Member>();

	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(final int version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Squad)) {
			return false;
		}
		Squad other = (Squad) obj;
		if (id != null) {
			if (!id.equals(other.id)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public SquadType getType() {
		return type;
	}

	public void setType(SquadType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		String result = "";
		if (name != null && !name.trim().isEmpty())
			result += "" + name;
		result += ", type: "+type;
		return result;
	}

	public Member getLeaderMale() {
		return this.leaderMale;
	}

	public void setLeaderMale(final Member leaderMale) {
		this.leaderMale = leaderMale;
	}

	public Member getLeaderFemale() {
		return leaderFemale;
	}

	public void setLeaderFemale(Member leaderFemale) {
		this.leaderFemale = leaderFemale;
	}

	public Set<Member> getAssistants() {
		return this.assistants;
	}

	public void setAssistants(final Set<Member> assistants) {
		this.assistants = assistants;
	}
	
	@Override
	public int compareTo(Squad o) {
		if (type != null && o.type != null) 
			return type.compareTo(o.type);
		if (id != null && o.id != null)
			return id.compareTo(o.id);
		return this.compareTo(o);
	}

}