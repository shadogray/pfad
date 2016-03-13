/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Version;

import org.hibernate.envers.Audited;

@Audited(withModifiedFlag = true)
@Entity
public class Function implements PrimaryKeyHolder, Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "function_seq")
	@SequenceGenerator(name = "function_seq", sequenceName = "function_seq", allocationSize = 1, initialValue = 1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@Column
	private String function;

	@Column
	private String key;

	@Column
	private boolean exportReg;

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
		if (!(obj instanceof Function)) {
			return false;
		}
		Function other = (Function) obj;
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

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean isExportReg() {
		return exportReg;
	}

	public void setExportReg(boolean exportReg) {
		this.exportReg = exportReg;
	}

	@Override
	public String toString() {
		String result = "" + function;
		if (key != null && !key.trim().isEmpty())
			result += ", key: " + key;
		result += ", reg: " + exportReg;
		return result;
	}
}
