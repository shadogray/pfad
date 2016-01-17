/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import java.io.Serializable;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.Version;

import at.tfr.pfad.ConfigurationType;
import at.tfr.pfad.view.Role;

@Entity
public class Configuration implements PrimaryKeyHolder, Serializable {

	public static final String BADEN_KEY = "BAD";
	public static final String BADEN_KEYPFX = "3-BAD-";
	public static final String REGEND_KEY = "RegistrationEnd";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "configuration_seq")
	@SequenceGenerator(name = "configuration_seq", sequenceName = "configuration_seq", allocationSize = 1, initialValue = 1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@Column(length = 64)
	private String ckey;

	@Column(length = 4096)
	private String cvalue;

	@Column
	@Enumerated(EnumType.STRING)
	private Role role;

	@Column(nullable=false, columnDefinition="varchar2(16) default 'simple' not null")
	@Enumerated(EnumType.STRING)
	private ConfigurationType type;

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

	public Role getRole() {
		return role != null ? role : Role.none;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public ConfigurationType getType() {
		return type != null ? type : ConfigurationType.simple;
	}

	public void setType(ConfigurationType type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Configuration)) {
			return false;
		}
		Configuration other = (Configuration) obj;
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

	public String getCkey() {
		return ckey;
	}

	public void setCkey(String ckey) {
		this.ckey = ckey;
	}

	public String getCvalue() {
		return cvalue;
	}

	public void setCvalue(String cvalue) {
		this.cvalue = cvalue;
	}

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (ckey != null && !ckey.trim().isEmpty())
			result += "ckey: " + ckey;
		if (cvalue != null && !cvalue.trim().isEmpty())
			result += ", cvalue: " + cvalue;
		return result;
	}
}