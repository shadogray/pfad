/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
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
import org.hibernate.envers.AuditMappedBy;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import at.tfr.pfad.Pfad;
import at.tfr.pfad.ScoutRole;
import at.tfr.pfad.Sex;
import at.tfr.pfad.dao.AuditListener;

@NamedQueries({ 
	@NamedQuery(name = "Member.distName", query = "select distinct m.name from Member m where m.name is not null order by m.name"),
	@NamedQuery(name = "Member.distNameLike", query = "select distinct m.name from Member m where lower(m.name) like ?1 order by m.name"),
	@NamedQuery(name = "Member.distVorname", query = "select distinct m.vorname from Member m where m.vorname is not null order by m.vorname"),
	@NamedQuery(name = "Member.distVornameLike", query = "select distinct m.vorname from Member m where lower(m.vorname) like ?1 order by m.vorname"),
	@NamedQuery(name = "Member.distPLZ", query = "select distinct m.plz from Member m where m.plz is not null order by m.plz"),
	@NamedQuery(name = "Member.distPLZLike", query = "select distinct m.plz from Member m where lower(m.plz) like ?1 order by m.plz"),
	@NamedQuery(name = "Member.distOrt", query = "select distinct m.ort from Member m where m.ort is not null order by m.ort"),
	@NamedQuery(name = "Member.distOrtLike", query = "select distinct m.ort from Member m where lower(m.ort) like ?1 order by m.ort"),
	@NamedQuery(name = "Member.distStrasse", query = "select distinct m.strasse from Member m where m.strasse is not null order by m.strasse"),
	@NamedQuery(name = "Member.distStrasseLike", query = "select distinct m.strasse from Member m where lower(m.strasse) like ?1 order by m.strasse"),
	@NamedQuery(name = "Member.distTitel", query = "select distinct m.titel from Member m where m.titel is not null order by m.titel"),
	@NamedQuery(name = "Member.distTitelLike", query = "select distinct m.titel from Member m where lower(m.titel) like ?1 order by m.titel"),
	@NamedQuery(name = "Member.distAnrede", query = "select distinct m.anrede from Member m where m.anrede is not null order by m.anrede"),
	@NamedQuery(name = "Member.distAnredeLike", query = "select distinct m.anrede from Member m where lower(m.anrede) like ?1 order by m.anrede"),
	@NamedQuery(name = "Member.distReligion", query = "select distinct m.religion from Member m where m.religion is not null order by m.religion"),
	@NamedQuery(name = "Member.distReligionLike", query = "select distinct m.religion from Member m where lower(m.religion) like ?1 order by m.religion"), 
	@NamedQuery(name = "Member.withFunction", query = "select m from Member m where ?1 member of m.funktionen"),
	})
@NamedEntityGraphs({
		@NamedEntityGraph(name = "fetchAll", 
				attributeNodes = { @NamedAttributeNode("funktionen"),
				@NamedAttributeNode("Vollzahler"), 
				@NamedAttributeNode("reduced"), 
				@NamedAttributeNode("parents"),
				@NamedAttributeNode("siblings"), 
				@NamedAttributeNode("trupp") }),
		@NamedEntityGraph(name = "withTrupp", attributeNodes = @NamedAttributeNode("trupp")) })
@Audited(withModifiedFlag = true)
@Entity
@EntityListeners({ AuditListener.class })
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonIgnoreProperties(ignoreUnknown = true, value = { "handler", "hibernateLazyInitializer" })
public class Member extends BaseEntity implements Comparable<Member>, Auditable, Presentable {

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

	@Column(columnDefinition = "boolean default 'false' not null")
	protected boolean dead;

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

	@ManyToOne
	@OrderBy("name")
	protected Squad trupp;

	@NotAudited
	@Column(insertable = false, updatable = false, name = "Trupp_id")
	protected Long truppId;

	@NotAudited // inverse side!
	@ManyToMany(mappedBy = "assistants")
	protected Set<Squad> squads;

	@NotAudited // inverse side!
	@OneToMany(mappedBy = "leaderFemale")
	protected Set<Squad> femaleGuided;

	@NotAudited // inverse side!
	@OneToMany(mappedBy = "leaderMale")
	protected Set<Squad> maleGuided;

	@ManyToOne
	protected Member Vollzahler;

	@NotAudited // inverse side!
	@OneToMany(mappedBy = "Vollzahler")
	@OrderBy("Name, Vorname")
	protected Set<Member> reduced;

	@NotAudited
	@Column(insertable = false, updatable = false, name = "Vollzahler_id")
	protected Long VollzahlerId;

	@Audited
	@ManyToMany
	protected Set<Function> funktionen = new HashSet<>();

	@Audited
	@ManyToMany
	@JoinTable(name = "member_member", joinColumns = @JoinColumn(name = "member_id"), inverseJoinColumns = @JoinColumn(name = "siblings_id"))
	@OrderBy("Name, Vorname")
	protected Set<Member> siblings = new HashSet<>();

	@NotAudited // inverse side!
	@ManyToMany(mappedBy = "siblings")
	@OrderBy("Name, Vorname")
	protected Set<Member> parents = new TreeSet<>();

	@NotAudited // inverse side!
	@OneToMany(mappedBy = "payer")
	@OrderBy(value = "id DESC")
	private Set<Payment> payments;

	@NotAudited // inverse side!
	@OneToMany(mappedBy = "member")
	@OrderBy(value = "id DESC")
	private Set<Booking> bookings = new TreeSet<>();

	@NotAudited // inverse side!
	@OneToMany(mappedBy = "member")
	@OrderBy(value = "id DESC")
	private Set<Participation> participations = new TreeSet<>();
	
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

	public void setTrupp(final Squad trupp) {
		this.trupp = trupp;
		if (trupp != null) {
			aktiv = true;
		}
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
		if (this.funktionen != null && this.funktionen.stream().anyMatch(f -> Boolean.TRUE.equals(f.getExportReg()))) {
			aktiv = true;
		}
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

	protected void setParents(Set<Member> parents) {
		this.parents = parents;
	}
	
	public Member addParents(Collection<Member> parents) {
		parents.iterator().forEachRemaining(p -> addParent(p));
		return this;
	}
	
	public Member addParent(Member parent) {
		parent.getSiblings().add(this);
		getParents().add(parent);
		return this;
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

	public String toUiString() {
		StringBuilder result = new StringBuilder("Person:\n");
		if (bvKey != null && !bvKey.trim().isEmpty())
			result.append("\tBV-Key: \t").append(bvKey).append("\n");
		result.append("\nName:   \n");
		result.append("\tAnrede:  \t").append(anrede).append("\n");
		result.append("\tTitel:   \t").append(titel).append("\n");
		result.append("\tName:    \t").append(name).append("\n");
		result.append("\tVorname: \t").append(vorname).append("\n");
		
		// Address
		result.append("\nAdresse:\n");
		result.append("\tStrasse: \t").append(strasse).append("\n");
		result.append("\tPLZ:     \t").append(plz).append("\n");
		result.append("\tOrt:     \t").append(ort).append("\n");

		// Communications
		result.append("\nKontakt:\n");
		result.append("\tEmail:   \t").append(email).append("\n");
		result.append("\tTelefon: \t").append(telefon).append("\n");
		
		// GebDatum
		result.append("\nPersönliches:\n");
		result.append("\tGebTag:  \t").append(geburtstag().toString("dd.MM.yyyy")).append("\n");
		result.append("\tGeschlecht:\t").append(geschlecht).append("\n");
		result.append("\tAktiv:   \t").append(aktiv).append("\n");
		result.append("\tAktivExtern:\t").append(aktivExtern).append("\n");
		result.append("\tTrupp:   \t").append(trupp).append("\n");
		result.append("\tVollzahler:\t").append(Vollzahler).append("\n");
		
		return result.toString();
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
	
	@XmlTransient
	public Set<Participation> getParticipations() {
		return participations;
	}
	
	@XmlTransient
	public List<Long> getParticipationIds() {
		return participations.stream().map(Participation::getId).collect(Collectors.toList());
	}

	public void setParticipations(Set<Participation> participations) {
		this.participations = participations;
	}

	@Pfad
	public boolean isFree() {
		return free;
	}

	public void setFree(boolean free) {
		this.free = free;
	}

	public boolean isAnyFree() {
		return free || (funktionen != null && funktionen.stream().anyMatch(f->Boolean.TRUE.equals(f.getFree())));
	}
	
	@Pfad
	public boolean isDead() {
		return dead;
	}
	
	public void setDead(boolean dead) {
		this.dead = dead;
	}
	
	public DateTime geburtstag() {
		return new DateTime(gebJahr, gebMonat > 0 ? gebMonat : 1, gebTag > 0 ? gebTag : 1, 0, 0);
	}
}