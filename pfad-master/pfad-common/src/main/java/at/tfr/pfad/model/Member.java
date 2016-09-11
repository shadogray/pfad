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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import at.tfr.pfad.Pfad;
import at.tfr.pfad.ScoutRole;
import at.tfr.pfad.Sex;
import at.tfr.pfad.dao.AuditListener;

@NamedQueries({ 
		@NamedQuery(name = "Member.distName", query = "select distinct m.name from Member m order by m.name"),
		@NamedQuery(name = "Member.distVorname", query = "select distinct m.vorname from Member m order by m.vorname"),
		@NamedQuery(name = "Member.distPLZ", query = "select distinct m.plz from Member m order by m.plz"),
		@NamedQuery(name = "Member.distOrt", query = "select distinct m.ort from Member m order by m.ort"),
		@NamedQuery(name = "Member.distStrasse", query = "select distinct m.strasse from Member m order by m.strasse"),
		@NamedQuery(name = "Member.distTitel", query = "select distinct m.titel from Member m order by m.titel"),
		@NamedQuery(name = "Member.distAnrede", query = "select distinct m.anrede from Member m order by m.anrede"),
		@NamedQuery(name = "Member.distReligion", query = "select distinct m.religion from Member m order by m.religion"), })
@NamedEntityGraphs({
		@NamedEntityGraph(name = "fetchAll", attributeNodes = { @NamedAttributeNode("funktionen"),
				@NamedAttributeNode("Vollzahler"), @NamedAttributeNode("reduced"), @NamedAttributeNode("parents"),
				@NamedAttributeNode("siblings"), @NamedAttributeNode("trupp") }),
		@NamedEntityGraph(name = "withTrupp", attributeNodes = @NamedAttributeNode("trupp")) })
@Audited(withModifiedFlag = true)
@Entity
@EntityListeners({ AuditListener.class })
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonIgnoreProperties(ignoreUnknown = true, value = { "handler", "hibernateLazyInitializer" })
public class Member implements PrimaryKeyHolder, Serializable, Comparable<Member>, Auditable, Presentable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq")
	@SequenceGenerator(name = "member_seq", sequenceName = "member_seq", allocationSize = 1, initialValue = 1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	protected int version;

	@Column
	protected String bvKey;

	@Column
	protected String gruppenSchluessel;

	@Column
	protected long personenKey;

	@Column
	protected String titel;

	@Column
	protected String name;

	@Column
	protected String vorname;

	@Column
	protected String anrede;

	@Column
	protected int gebTag;

	@Column
	protected int gebMonat;

	@Column
	protected int gebJahr;

	@Column
	protected String strasse;

	@Column
	protected String plz;

	@Column
	protected String ort;

	@Enumerated(EnumType.STRING)
	protected Sex geschlecht;

	@Column
	protected boolean aktiv;

	@Column
	protected boolean aktivExtern;

	@Column
	protected String email;

	@Column
	protected String religion;

	@Column
	protected String telefon;

	@Column(columnDefinition = "boolean default 'false' not null")
	protected boolean trail;

	@Column(columnDefinition = "boolean default 'false' not null")
	protected boolean gilde;

	@Column(columnDefinition = "boolean default 'false' not null")
	protected boolean altER;

	@Column(columnDefinition = "boolean default 'false' not null")
	protected boolean support;

	@Column(columnDefinition = "boolean default 'false' not null")
	protected boolean infoMail;

	@Column(columnDefinition = "boolean default 'false' not null")
	protected boolean free;

	@Enumerated(EnumType.STRING)
	protected ScoutRole rolle;

	@Column
	protected Date changed;

	@Column
	protected Date created;

	@Column
	protected String changedBy;

	@Column
	protected String createdBy;

	@ManyToOne(fetch = FetchType.EAGER)
	@OrderBy("name")
	protected Squad trupp;

	@NotAudited
	@Column(insertable = false, updatable = false, name = "Trupp_id")
	protected Long truppId;

	@NotAudited
	@ManyToMany(mappedBy = "assistants")
	protected Set<Squad> squads;

	@NotAudited
	@OneToMany(mappedBy = "leaderFemale")
	protected Set<Squad> femaleGuided;

	@NotAudited
	@OneToMany(mappedBy = "leaderMale")
	protected Set<Squad> maleGuided;

	@ManyToOne
	protected Member Vollzahler;

	@OneToMany(mappedBy = "Vollzahler")
	@OrderBy("Name, Vorname")
	protected Set<Member> reduced;

	@NotAudited
	@Column(insertable = false, updatable = false, name = "Vollzahler_id")
	protected Long VollzahlerId;

	@ManyToMany(fetch = FetchType.EAGER)
	protected Set<Function> funktionen = new HashSet<>();

	@ManyToMany
	@JoinTable(name = "member_member", joinColumns = @JoinColumn(name = "member_id"), inverseJoinColumns = @JoinColumn(name = "siblings_id"))
	@OrderBy("Name, Vorname")
	protected Set<Member> siblings = new HashSet<>();

	@ManyToMany(mappedBy = "siblings")
	@OrderBy("Name, Vorname")
	protected Set<Member> parents = new HashSet<>();

	@OneToMany(mappedBy = "payer")
	@OrderBy(value = "id DESC")
	private Set<Payment> payments;

	@OneToMany(mappedBy = "member")
	@OrderBy(value = "id DESC")
	private Set<Booking> bookings = new HashSet<Booking>();

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

	@Pfad
	public String getBVKey() {
		if (id == null) {
			return bvKey;
		}
		return StringUtils.isEmpty(bvKey) ? Configuration.BADEN_KEYPFX + id : bvKey;
	}

	public void setBVKey(String BVKey) {
		this.bvKey = BVKey;
	}

	public String getGruppenSchluessel() {
		return StringUtils.isEmpty(gruppenSchluessel) ? Configuration.BADEN_KEY : gruppenSchluessel;
	}

	public void setGruppenSchluessel(String GruppenSchluessel) {
		if (Configuration.BADEN_KEY.equals(bvKey))
			return;
		this.gruppenSchluessel = GruppenSchluessel;
	}

	public long getPersonenKey() {
		return (personenKey == 0 && id != null) ? id : personenKey;
	}

	public void setPersonenKey(long PersonenKey) {
		this.personenKey = PersonenKey;
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
	public boolean isAktivExtern() {
		return aktivExtern;
	}

	public void setAktivExtern(boolean aktivExtern) {
		this.aktivExtern = aktivExtern;
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

	@Pfad
	public boolean isTrail() {
		return trail;
	}

	public void setTrail(boolean Trail) {
		this.trail = Trail;
	}

	@Pfad
	public boolean isGilde() {
		return gilde;
	}

	public void setGilde(boolean Gilde) {
		this.gilde = Gilde;
	}

	@Pfad
	public boolean isAltER() {
		return altER;
	}

	public void setAltER(boolean AltER) {
		this.altER = AltER;
	}

	@Pfad
	public ScoutRole getRolle() {
		return rolle;
	}

	public void setRolle(ScoutRole Rolle) {
		this.rolle = Rolle;
	}

	@Pfad
	public boolean isInfoMail() {
		return infoMail;
	}

	public void setInfoMail(boolean infoMail) {
		this.infoMail = infoMail;
	}

	@Pfad
	public boolean isSupport() {
		return support;
	}

	public void setSupport(boolean support) {
		this.support = support;
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

	public Squad getTrupp() {
		return this.trupp;
	}

	public void setTrupp(final Squad Trupp) {
		this.trupp = Trupp;
	}

	@Pfad
	public Member getVollzahler() {
		return this.Vollzahler;
	}

	public void setVollzahler(final Member Vollzahler) {
		this.Vollzahler = Vollzahler;
	}

	@Pfad
	public Set<Function> getFunktionen() {
		Iterator<Function> fi = this.funktionen.iterator();
		while (fi.hasNext()) {
			Function f = fi.next();
			if (f != null && f.getId() == null)
				fi.remove();
		}
		return this.funktionen;
	}

	public List<Long> getFunktionenIds() {
		return funktionen.stream().map(Function::getId).collect(Collectors.toList());
	}

	public void setFunktionen(final Set<Function> Funktionen) {
		this.funktionen = Funktionen;
	}

	@Pfad
	public Set<Member> getSiblings() {
		return siblings;
	}

	public List<Long> getSiblingIds() {
		return siblings.stream().map(Member::getId).collect(Collectors.toList());
	}

	public void setSiblings(Set<Member> siblings) {
		this.siblings = siblings;
	}

	/**
	 * Inverse relationship, add will not be persisted.
	 * 
	 * @return
	 */
	public Set<Member> getParents() {
		return parents;
	}

	public void setParents(Set<Member> parents) {
		this.parents = parents;
	}

	@Override
	public int compareTo(Member o) {
		return getCompareString().compareTo(o.getCompareString());
	}

	public String getCompareString() {
		return (("" + name) + vorname) + id;
	}

	public Long getTruppId() {
		return truppId;
	}

	public Long getVollzahlerId() {
		return VollzahlerId;
	}

	/**
	 * Inverse relationship, add will not be persisted.
	 * 
	 * @return
	 */
	@XmlTransient
	public Set<Payment> getPayments() {
		return payments;
	}

	@XmlTransient
	public List<Long> getPaymentsIds() {
		return payments.stream().map(Payment::getId).collect(Collectors.toList());
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
		result += free ? ":FREI" : "";
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
		if (bvKey != null && !bvKey.trim().isEmpty())
			result += "bvKey: " + bvKey;
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
		result += ", aktivExtern: " + aktivExtern;
		if (email != null && !email.trim().isEmpty())
			result += ", email: " + email;
		if (religion != null && !religion.trim().isEmpty())
			result += ", religion: " + religion;
		if (telefon != null && !telefon.trim().isEmpty())
			result += ", telefon: " + telefon;
		if (truppId != null)
			result += ", truppId: " + truppId;
		if (VollzahlerId != null)
			result += ", VollzahlerId: " + VollzahlerId;
		return result;
	}

	/**
	 * Inverse relationship, add will not be persisted.
	 * 
	 * @return
	 */
	@XmlTransient
	public Set<Booking> getBookings() {
		return this.bookings;
	}

	@XmlTransient
	public List<Long> getBookingsIds() {
		return bookings.stream().map(Booking::getId).collect(Collectors.toList());
	}

	public void setBookings(final Set<Booking> bookings) {
		this.bookings = bookings;
	}

	@Pfad
	public boolean isFree() {
		return free;
	}

	public void setFree(boolean free) {
		this.free = free;
	}

	public boolean isAnyFree() {
		return free || (funktionen != null && funktionen.stream().anyMatch(Function::isFree));
	}
}