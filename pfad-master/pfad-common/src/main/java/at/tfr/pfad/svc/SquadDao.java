/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.svc;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Id;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import at.tfr.pfad.SquadType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SquadDao extends BaseDao implements Comparable<SquadDao> {

	@Id
	@XmlID
	private Long id;
	private int version;
	private SquadType type;
	private String name;
	private String login;
	private Date changed;
	private Date created;
	private String changedBy;
	private String createdBy;
	private BaseDao leaderMale;
	private BaseDao leaderFemale;
	private Set<BaseDao> assistants = new HashSet<BaseDao>();
	private Set<BaseDao> scouts = new HashSet<BaseDao>();
 
	public Long getId() {
		return this.id;
	}

	public String getIdStr() {
		return id != null ? id.toString() : "";
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
		if (!(obj instanceof SquadDao)) {
			return false;
		}
		SquadDao other = (SquadDao) obj;
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

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public Date getChanged() {
		return changed;
	}

	public void setChanged(Date changed) {
		this.changed = changed;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public String toString() {
		String result = "";
		if (name != null && !name.trim().isEmpty())
			result += "" + name;
		result += ", " + type;
		return result;
	}

	public BaseDao getLeaderMale() {
		return this.leaderMale;
	}

	public void setLeaderMale(final BaseDao leaderMale) {
		this.leaderMale = leaderMale;
	}

	public BaseDao getLeaderFemale() {
		return leaderFemale;
	}

	public void setLeaderFemale(BaseDao leaderFemale) {
		this.leaderFemale = leaderFemale;
	}

	@XmlTransient
	public Set<BaseDao> getAssistants() {
		return this.assistants;
	}

	public void setAssistants(final Set<BaseDao> assistants) {
		this.assistants = assistants;
	}

	@XmlTransient
	public Set<BaseDao> getScouts() {
		return scouts;
	}

	@Override
	public int compareTo(SquadDao o) {
		if (type != null && o.type != null)
			return type.compareTo(o.type);
		if (id != null && o.id != null)
			return id.compareTo(o.id);
		return this.compareTo(o);
	}

}