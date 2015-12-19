/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Version;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import at.tfr.pfad.ScoutRole;
import at.tfr.pfad.Sex;
import at.tfr.pfad.dao.AuditListener;

@NamedQueries({ @NamedQuery(name = "distName", query = "select distinct m.Name from Member m order by m.Name"),
		@NamedQuery(name = "distVorname", query = "select distinct m.Vorname from Member m order by m.Vorname"),
		@NamedQuery(name = "distPLZ", query = "select distinct m.PLZ from Member m order by m.PLZ"),
		@NamedQuery(name = "distOrt", query = "select distinct m.Ort from Member m order by m.Ort"),
		@NamedQuery(name = "distStrasse", query = "select distinct m.Strasse from Member m order by m.Strasse"),
		@NamedQuery(name = "distTitel", query = "select distinct m.Titel from Member m order by m.Titel"),
		@NamedQuery(name = "distAnrede", query = "select distinct m.Anrede from Member m order by m.Anrede"),
		@NamedQuery(name = "distReligion", query = "select distinct m.Religion from Member m order by m.Religion"), })

@Audited(targetAuditMode=RelationTargetAuditMode.NOT_AUDITED,withModifiedFlag=true)
@Entity
@EntityListeners({AuditListener.class})
public class Member implements Serializable, Comparable<Member>, Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq")
	@SequenceGenerator(name = "member_seq", sequenceName = "member_seq", allocationSize = 1, initialValue = 1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	protected int version;

	@Column
	protected String BVKey;

	@Column
	protected String GruppenSchluessel;

	@Column
	protected long PersonenKey;

	@Column
	protected String Titel;

	@Column
	protected String Name;

	@Column
	protected String Vorname;

	@Column
	protected String Anrede;

	@Column
	protected int GebTag;

	@Column
	protected int GebMonat;

	@Column
	protected int GebJahr;

	@Column
	protected String Strasse;

	@Column
	protected String PLZ;

	@Column
	protected String Ort;

	@Enumerated(EnumType.STRING)
	protected Sex Geschlecht;

	@Column
	protected boolean Aktiv;

	@Column
	protected String Email;

	@Column
	protected String Religion;

	@Column
	protected String Telefon;

	@Column
	protected boolean Trail;

	@Column
	protected boolean Gilde;

	@Column
	protected boolean AltER;
	
	@Column
	protected boolean Support;
	
	@Column 
	protected boolean InfoMail;

	@Enumerated(EnumType.STRING)
	protected ScoutRole Rolle;

	@Column
	protected Date changed;
	
	@Column
	protected Date created;
	
	@Column
	protected String changedBy;
	
	@Column
	protected String createdBy;
	
	@ManyToOne
	@OrderBy("name")
	protected Squad Trupp;
	
	@NotAudited
	@Column(insertable=false, updatable=false, name="Trupp_id")
	protected Long TruppId;

	@ManyToOne
	@OrderBy("name, vorname")
	protected Member Vollzahler;
	
	@NotAudited
	@Column(insertable=false, updatable=false, name="Vollzahler_id")
	protected Long VollzahlerId;

	@ManyToMany
	protected Set<Function> Funktionen = new HashSet<>();

	@NotAudited
	@ManyToMany
	@OrderBy("name, vorname")
	protected Set<Member> siblings;
	
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
		if (!(obj instanceof Member)) {
			return false;
		}
		Member other = (Member) obj;
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

	public String getBVKey() {
		if (id == null) {
			return BVKey;
		}
		return BVKey;
		//return StringUtils.isEmpty(BVKey) ? Configuration.BADEN_KEYPFX+id : BVKey;
	}

	public void setBVKey(String BVKey) {
		this.BVKey = BVKey;
	}

	public String getGruppenSchluessel() {
		return GruppenSchluessel;
		//return StringUtils.isEmpty(GruppenSchluessel) ? Configuration.BADEN_KEY : GruppenSchluessel;
	}

	public void setGruppenSchluessel(String GruppenSchluessel) {
//		if (Configuration.BADEN_KEY.equals(BVKey)) 
//			return;
		this.GruppenSchluessel = GruppenSchluessel;
	}

	public long getPersonenKey() {
		return PersonenKey;
		//		return PersonenKey == 0 && id != null ? id : PersonenKey;
	}

	public void setPersonenKey(long PersonenKey) {
		this.PersonenKey = PersonenKey;
	}

	public String getTitel() {
		return Titel;
	}

	public void setTitel(String Titel) {
		this.Titel = Titel;
	}

	public String getName() {
		return Name;
	}

	public void setName(String Name) {
		this.Name = Name;
	}

	public String getVorname() {
		return Vorname;
	}

	public void setVorname(String Vorname) {
		this.Vorname = Vorname;
	}

	public String getAnrede() {
		return Anrede;
	}

	public void setAnrede(String Anrede) {
		this.Anrede = Anrede;
	}

	public int getGebTag() {
		return GebTag;
	}

	public void setGebTag(int GebTag) {
		this.GebTag = GebTag;
	}

	public int getGebMonat() {
		return GebMonat;
	}

	public void setGebMonat(int GebMonat) {
		this.GebMonat = GebMonat;
	}

	public int getGebJahr() {
		return GebJahr;
	}

	public void setGebJahr(int GebJahr) {
		this.GebJahr = GebJahr;
	}

	public String getStrasse() {
		return Strasse;
	}

	public void setStrasse(String Strasse) {
		this.Strasse = Strasse;
	}

	public String getPLZ() {
		return PLZ;
	}

	public void setPLZ(String PLZ) {
		this.PLZ = PLZ;
	}

	public String getOrt() {
		return Ort;
	}

	public void setOrt(String Ort) {
		this.Ort = Ort;
	}

	public Sex getGeschlecht() {
		return Geschlecht;
	}

	public void setGeschlecht(Sex Geschlecht) {
		this.Geschlecht = Geschlecht;
	}

	public boolean isAktiv() {
		return Aktiv;
	}

	public void setAktiv(boolean Aktiv) {
		this.Aktiv = Aktiv;
	}

	public String getEmail() {
		return Email;
	}

	public void setEmail(String Email) {
		this.Email = Email;
	}

	public String getReligion() {
		return Religion;
	}

	public void setReligion(String Religion) {
		this.Religion = Religion;
	}

	public String getTelefon() {
		return Telefon;
	}

	public void setTelefon(String Telefon) {
		this.Telefon = Telefon;
	}

	public boolean isTrail() {
		return Trail;
	}

	public void setTrail(boolean Trail) {
		this.Trail = Trail;
	}

	public boolean isGilde() {
		return Gilde;
	}

	public void setGilde(boolean Gilde) {
		this.Gilde = Gilde;
	}

	public boolean isAltER() {
		return AltER;
	}

	public void setAltER(boolean AltER) {
		this.AltER = AltER;
	}

	public ScoutRole getRolle() {
		return Rolle;
	}

	public void setRolle(ScoutRole Rolle) {
		this.Rolle = Rolle;
	}

	public boolean isInfoMail() {
		return InfoMail;
	}

	public void setInfoMail(boolean infoMail) {
		InfoMail = infoMail;
	}

	public boolean isSupport() {
		return Support;
	}

	public void setSupport(boolean support) {
		Support = support;
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
	public String toString() {
		String result = "";
		if (BVKey != null && !BVKey.trim().isEmpty())
			result += "" + BVKey;
		if (Name != null && !Name.trim().isEmpty())
			result += ", " + Name;
		if (Vorname != null && !Vorname.trim().isEmpty())
			result += ", " + Vorname;
		result += ", " + GebJahr;
		result += ", Aktiv: " + Aktiv;
		return result;
	}

	public Squad getTrupp() {
		return this.Trupp;
	}

	public void setTrupp(final Squad Trupp) {
		this.Trupp = Trupp;
	}

	public Member getVollzahler() {
		return this.Vollzahler;
	}

	public void setVollzahler(final Member Vollzahler) {
		this.Vollzahler = Vollzahler;
	}

	public Set<Function> getFunktionen() {
		Iterator<Function> fi = this.Funktionen.iterator();
		while (fi.hasNext()) {
			Function f = fi.next();
			if (f != null && f.getId() == null)
				fi.remove();
		}
		return this.Funktionen;
	}

	public void setFunktionen(final Set<Function> Funktionen) {
		this.Funktionen = Funktionen;
	}

	public Set<Member> getSiblings() {
		return siblings;
	}

	public void setSiblings(Set<Member> siblings) {
		this.siblings = siblings;
	}

	@Override
	public int compareTo(Member o) {
		return getCompareString().compareTo(o.getCompareString());
	}

	public String getCompareString() {
		return (("" + Name) + Vorname) + id;
	}

	public Long getTruppId() {
		return TruppId;
	}
	
	public Long getVollzahlerId() {
		return VollzahlerId;
	}
}