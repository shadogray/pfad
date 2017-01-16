package at.tfr.pfad.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NamedSubgraph;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import at.tfr.pfad.BookingStatus;
import at.tfr.pfad.dao.AuditListener;

@Audited(withModifiedFlag = true)
@NamedQueries({
		@NamedQuery(name = "BookingsForPayment", query = "select b from Booking b where ?1 member of b.payments order by b.id"),
		@NamedQuery(name = "BookingsForPaymentIds", query = "select b,m,a,s from Booking b left join b.member m left join b.activity a left join b.squad s , Payment p where p member of b.payments and p.id in ?1 order by b.id, p.id"),
		})
@NamedEntityGraphs({ 
	@NamedEntityGraph(name = Booking.Booking, attributeNodes = { 
			@NamedAttributeNode("activity"),
			@NamedAttributeNode("member"),
			@NamedAttributeNode("squad"),
			}), 
	@NamedEntityGraph(name = Booking.FetchAll, attributeNodes = { 
			@NamedAttributeNode("activity"),
			@NamedAttributeNode("member"),
			@NamedAttributeNode("squad"),
			@NamedAttributeNode("payments"),
			}), 
	})
@Entity
@EntityListeners({ AuditListener.class })
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonIgnoreProperties(ignoreUnknown = true, value = { "handler", "hibernateLazyInitializer" })
public class Booking implements PrimaryKeyHolder, Auditable, Serializable, Presentable, Comparable<Booking> {

	public static final String Booking = "BookingBooking";
	public static final String FetchAll = "BookingFetchAll";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_seq")
	@SequenceGenerator(name = "booking_seq", sequenceName = "booking_seq", allocationSize = 1, initialValue = 1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@ManyToMany(mappedBy = "bookings")
	@BatchSize(size = 10)
	private Set<Payment> payments = new HashSet<Payment>();

	@ManyToOne(optional = false)
	private Member member;

	@Column(name = "member_id", insertable = false, updatable = false)
	private Long memberId;

	@ManyToOne(optional = false)
	private Activity activity;

	@Column(name = "activity_id", insertable = false, updatable = false)
	private Long activityId;

	@ManyToOne(fetch = FetchType.LAZY)
	private Squad squad;

	@Column(name = "squad_id", insertable = false, updatable = false)
	private Long squadId;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private BookingStatus status;

	@Column
	private String comment;

	@Column
	protected Date changed;

	@Column
	protected Date created;

	@Column
	protected String changedBy;

	@Column
	protected String createdBy;

	public Booking() {
	}
	
	public Booking(Member member, Activity activity) {
		this.member = member;
		this.activity = activity;
		this.status = BookingStatus.created;
	}
	
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
		if (!(obj instanceof Booking)) {
			return false;
		}
		Booking other = (Booking) obj;
		return checkIds(id, other);
	}

	@Override
	public int hashCode() {
		return 31 + ((id == null) ? 0 : id.hashCode());
	}

	@XmlTransient
	public Set<Payment> getPayments() {
		return this.payments;
	}

	@XmlTransient
	public List<Long> getPaymentsIds() {
		return payments.stream().map(Payment::getId).collect(Collectors.toList());
	}

	public void setPayments(final Set<Payment> payments) {
		this.payments = payments;
	}

	@XmlTransient
	public List<Payment> getSortedPayments() {
		return payments.stream().sorted(new PkComparator<Payment>()).collect(Collectors.toList());
	}

	public Member getMember() {
		return this.member;
	}

	public void setMember(final Member member) {
		this.member = member;
	}

	public Long getMemberId() {
		return memberId;
	}

	public Activity getActivity() {
		return this.activity;
	}

	public void setActivity(final Activity activity) {
		this.activity = activity;
	}

	public Long getActivityId() {
		return activityId;
	}

	public Squad getSquad() {
		return squad;
	}

	public void setSquad(Squad squad) {
		this.squad = squad;
	}

	public Long getSquadId() {
		return squadId;
	}

	public BookingStatus getStatus() {
		return status;
	}

	public void setStatus(BookingStatus status) {
		this.status = status;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
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

	@Transient
	public boolean isValid() {
		return !BookingStatus.storno.equals(status);
	}

	@Override
	public String toString() {
		String result = (activity != null
				? activity.getType() + ":" + activity.getName() + "/" + activity.getStartString()
				: getClass().getSimpleName());
		if (member != null) {
			result += " " + member.getName() + " " + member.getVorname() + ", " + member.getGebJahr() + ", "
					+ member.getPLZ() + ", " + member.getStrasse();
		}
		if (comment != null && !comment.trim().isEmpty())
			result += " " + comment;
		return result;
	}

	@Override
	public String getName() {
		return getShortString();
	}

	@Override
	public int compareTo(Booking o) {
		if (this.id != null && o.id != null)
			return this.id.compareTo(o.id);
		return this.getShortString().compareTo(o.getShortString());
	}

	@Override
	public String getShortString() {
		String result = (activity != null ? activity.getName() : "undef");
		if (member != null) {
			result += " " + member.getName() + " " + member.getVorname() + ", " + member.getGebJahr();
		}
		return result;
	}

	@Override
	public String getLongString() {
		return toString();
	}
}