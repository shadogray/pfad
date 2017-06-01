/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.KeyValue;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import at.tfr.pfad.Pfad;
import at.tfr.pfad.Sex;
import at.tfr.pfad.dao.AuditListener;

@NamedQueries({ 
		@NamedQuery(name = "Registration.distName", query = "select distinct r.name from Registration r order by r.name"),
		@NamedQuery(name = "Registration.distVorname", query = "select distinct r.vorname from Registration r order by r.vorname"),
		@NamedQuery(name = "Registration.distPLZ", query = "select distinct r.plz from Registration r order by r.plz"),
		@NamedQuery(name = "Registration.distOrt", query = "select distinct r.ort from Registration r order by r.ort"),
		@NamedQuery(name = "Registration.distStrasse", query = "select distinct r.strasse from Registration r order by r.strasse"),
	})
@Audited(withModifiedFlag = true)
@Entity
@EntityListeners({ AuditListener.class })
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonIgnoreProperties(ignoreUnknown = true, value = { "handler", "hibernateLazyInitializer" })
public class Registration implements PrimaryKeyHolder, Serializable, Comparable<Registration>, Auditable, Presentable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "registration_seq")
	@SequenceGenerator(name = "registration_seq", sequenceName = "registration_seq", allocationSize = 1, initialValue = 1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	protected int version;

	@Column(length=16)
	protected String titel;

	@Column(length=128)
	protected String name;

	@Column(length=128)
	protected String vorname;

	@Column(length=128)
	protected String parentName;

	@Column(length=128)
	protected String parentVorname;

	@Column(length=16)
	protected String anrede;

	@Column
	protected int gebTag;

	@Column
	protected int gebMonat;

	@Column
	protected int gebJahr;

	@Column
	protected int schoolEntry;
	
	@Column(length=128)
	protected String strasse;

	@Column(length=16)
	protected String plz;

	@Column(length=128)
	protected String ort;

	@Enumerated(EnumType.STRING)
	protected Sex geschlecht;

	@Column(columnDefinition = "boolean default 'true' not null")
	protected boolean aktiv;

	@Column(length=128)
	protected String email;

	@Column(length=32)
	protected String religion;

	@Column
	protected String telefon;

	@Column(columnDefinition = "boolean default 'false' not null")
	protected boolean storno;

	@Column
	protected Date changed;

	@Column
	protected Date created;

	@Column(length=32)
	protected String changedBy;

	@Column(length=32)
	protected String createdBy;

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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Registration)) {
			return false;
		}
		Registration other = (Registration) obj;
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

	@Pfad
	public String getTitel() {
		return titel;
	}

	public void setTitel(String Titel) {
		this.titel = Titel;
	}

	@Pfad
	public String getName() {
		return name;
	}

	public void setName(String Name) {
		this.name = Name;
	}

	@Pfad
	public String getVorname() {
		return vorname;
	}

	public void setVorname(String Vorname) {
		this.vorname = Vorname;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String getParentVorname() {
		return parentVorname;
	}

	public void setParentVorname(String parentVorname) {
		this.parentVorname = parentVorname;
	}

	public boolean isStorno() {
		return storno;
	}

	public void setStorno(boolean storno) {
		this.storno = storno;
	}

	@Pfad
	public String getAnrede() {
		return anrede;
	}

	public void setAnrede(String Anrede) {
		this.anrede = Anrede;
	}

	@Pfad
	public int getGebTag() {
		return gebTag;
	}

	public void setGebTag(int GebTag) {
		this.gebTag = GebTag;
	}

	@Pfad
	public int getGebMonat() {
		return gebMonat;
	}

	public void setGebMonat(int GebMonat) {
		this.gebMonat = GebMonat;
	}

	@Pfad
	public int getGebJahr() {
		return gebJahr;
	}

	public void setGebJahr(int GebJahr) {
		this.gebJahr = GebJahr;
	}

	@Pfad
	public int getSchoolEntry() {
		return schoolEntry;
	}
	
	public void setSchoolEntry(int schoolEntry) {
		this.schoolEntry = schoolEntry;
	}
	
	@Pfad
	public String getStrasse() {
		return strasse;
	}

	public void setStrasse(String Strasse) {
		this.strasse = Strasse;
	}

	@Pfad
	public String getPLZ() {
		return plz;
	}

	public void setPLZ(String PLZ) {
		this.plz = PLZ;
	}

	@Pfad
	public String getOrt() {
		return ort;
	}

	public void setOrt(String Ort) {
		this.ort = Ort;
	}

	@Pfad
	public Sex getGeschlecht() {
		return geschlecht;
	}

	public void setGeschlecht(Sex Geschlecht) {
		this.geschlecht = Geschlecht;
	}

	@Pfad
	public boolean isAktiv() {
		return aktiv;
	}

	public void setAktiv(boolean Aktiv) {
		this.aktiv = Aktiv;
	}

	@Pfad
	public String getEmail() {
		return email;
	}

	public void setEmail(String Email) {
		this.email = Email;
	}

	@Pfad
	public String getReligion() {
		return religion;
	}

	public void setReligion(String Religion) {
		this.religion = Religion;
	}

	@Pfad
	public String getTelefon() {
		return telefon;
	}

	public void setTelefon(String Telefon) {
		this.telefon = Telefon;
	}

	@Override
	public Date getChanged() {
		return changed;
	}

	@Override
	public void setChanged(Date changed) {
		this.changed = changed;
	}

	@Override
	public Date getCreated() {
		return created;
	}

	@Override
	public void setCreated(Date created) {
		this.created = created;
	}

	@Override
	public String getChangedBy() {
		return changedBy;
	}

	@Override
	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}

	@Override
	public String getCreatedBy() {
		return createdBy;
	}

	@Override
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public int compareTo(Registration o) {
		return getCompareString().compareTo(o.getCompareString());
	}

	public String getCompareString() {
		return (("" + name) + vorname) + id;
	}

	/**
	 * property for REST API
	 * 
	 * @return
	 */
	public String getShortString() {
		return toShortString();
	}

	public String toShortString() {
		String result = "";
		if (name != null && !name.trim().isEmpty())
			result += name;
		if (vorname != null && !vorname.trim().isEmpty())
			result += " " + vorname;
		result += ", " + gebJahr;
		if (ort != null && !ort.trim().isEmpty())
			result += ", " + ort;
		return result;
	}

	/**
	 * property for REST API
	 * 
	 * @return
	 */
	public String getLongString() {
		return toString();
	}

	@Override
	public String toString() {
		String result = "";
		// if (bvKey != null && !bvKey.trim().isEmpty())
		// result += bvKey;
		if (titel != null && !titel.trim().isEmpty())
			result += (result.length() > 0 ? ", " : "") + titel;
		if (name != null && !name.trim().isEmpty())
			result += (result.length() > 0 ? ", " : "") + name;
		if (vorname != null && !vorname.trim().isEmpty())
			result += (result.length() > 0 ? ", " : "") + vorname;
		result += (result.length() > 0 ? ", " : "") + gebTag + "." + gebMonat + "." + gebJahr;
		if (plz != null && !plz.trim().isEmpty())
			result += (result.length() > 0 ? ", " : "") + plz;
		if (ort != null && !ort.trim().isEmpty())
			result += " " + ort;
		result += (result.length() > 0 ? ", " : "") + (aktiv ? "aktiv" : "inaktiv");
		return result;
	}

	/**
	 * property for REST API
	 * 
	 * @return
	 */
	public String getFullString() {
		return toFullString();
	}

	public String toFullString() {
		String result = getClass().getSimpleName() + " ";
		if (titel != null && !titel.trim().isEmpty())
			result += ", titel: " + titel;
		if (name != null && !name.trim().isEmpty())
			result += ", name: " + name;
		if (vorname != null && !vorname.trim().isEmpty())
			result += ", vorname: " + vorname;
		if (anrede != null && !anrede.trim().isEmpty())
			result += ", anrede: " + anrede;
		result += ", gebTag: " + gebTag;
		result += ", gebMonat: " + gebMonat;
		result += ", gebJahr: " + gebJahr;
		if (strasse != null && !strasse.trim().isEmpty())
			result += ", strasse: " + strasse;
		if (plz != null && !plz.trim().isEmpty())
			result += ", plz: " + plz;
		if (ort != null && !ort.trim().isEmpty())
			result += ", ort: " + ort;
		result += ", aktiv: " + aktiv;
		if (email != null && !email.trim().isEmpty())
			result += ", email: " + email;
		if (religion != null && !religion.trim().isEmpty())
			result += ", religion: " + religion;
		if (telefon != null && !telefon.trim().isEmpty())
			result += ", telefon: " + telefon;
		return result;
	}
	
}