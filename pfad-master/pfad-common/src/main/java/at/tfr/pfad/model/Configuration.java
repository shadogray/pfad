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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import at.tfr.pfad.ConfigurationType;
import at.tfr.pfad.Role;

@Entity
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown=true, value = {"handler", "hibernateLazyInitializer"})
public class Configuration extends BaseEntity implements Comparable<Configuration> {

	public static final String BADEN_KEY = "BAD";
	public static final String BADEN_KEYPFX = "3-BAD-";
	public static final String REGEND_KEY = "RegistrationEnd";
	public static final String BADEN_IBANS = "BadenIBANs";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "configuration_seq")
	@SequenceGenerator(name = "configuration_seq", sequenceName = "configuration_seq", allocationSize = 1, initialValue = 1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@Length(max = 64)
	@Column(length = 64)
	private String ckey;

	@Length(max = 4096)
	@Column(length = 4096)
	private String cvalue;

	@Column
	@Enumerated(EnumType.STRING)
	private Role role;

	@Column(nullable = false, columnDefinition = "varchar2(16) default 'simple'")
	@Enumerated(EnumType.STRING)
	private ConfigurationType type;
	
	@Column(length = 4096)
	private String description;
	
	@Column(length = 1024)
	private String headers;
	
	@XmlID
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHeaders() {
		return headers;
	}

	public void setHeaders(String headers) {
		this.headers = headers;
	}
	
	public String[] toHeaders(final int count) {
		String[] result = new String[count];
		String[] vals = (""+headers).split(";");
		for (int i=0; i<count; i++) {
			result[i] = ((StringUtils.isNotBlank(headers) && vals.length > i) ? vals[i] : ""+i).trim();
		}
		return result;
	}

	public String getCkey() {
		return ckey;
	}

	public void setCkey(String ckey) {
		this.ckey = ckey;
	}

	public String getCvalue() {
		if ((""+ckey).toLowerCase().contains("password")) 
			return "********";
		return cvalue;
	}

	public void setCvalue(String cvalue) {
		this.cvalue = cvalue;
	}

	public String getCvalueIntern() {
		return cvalue;
	}
	
	@Override
	public int compareTo(Configuration o) {
		if (this.id != null && o.id != null) 
			return this.id.compareTo(o.id);
		return this.toString().compareTo(o.toString());
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
	
	public String toTitle() {
		return ckey + ": " + (description != null ? description + "\n" : "") + "\nAbfrage: \n" + cvalue;
	}
}