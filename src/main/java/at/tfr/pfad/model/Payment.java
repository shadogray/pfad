package at.tfr.pfad.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.envers.Audited;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import at.tfr.pfad.PaymentType;
import at.tfr.pfad.dao.AuditListener;

@Audited(withModifiedFlag = true)
@Entity
@EntityListeners({AuditListener.class})
public class Payment implements PrimaryKeyHolder, Serializable, Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;
	
	@Column(nullable=false, columnDefinition="varchar2(16) default 'Membership' not null")
	@Enumerated(EnumType.STRING)
	private PaymentType type;

	@ManyToOne
	private Member payer;

	private Float amount;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date paymentDate;

	@Column
	private Boolean finished;

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

	@ManyToMany
	private Set<Booking> bookings = new HashSet<Booking>();

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
		if (!(obj instanceof Payment)) {
			return false;
		}
		Payment other = (Payment) obj;
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

	public Member getPayer() {
		return payer;
	}

	public void setPayer(Member Payer) {
		this.payer = Payer;
	}

	public Float getAmount() {
		return amount;
	}

	public void setAmount(Float amount) {
		this.amount = amount;
	}

	public Boolean getFinished() {
		return finished;
	}

	public void setFinished(Boolean finished) {
		this.finished = finished;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Set<Booking> getBookings() {
		return this.bookings;
	}

	public void setBookings(final Set<Booking> bookings) {
		this.bookings = bookings;
	}

	public PaymentType getType() {
		return type;
	}

	public void setType(PaymentType type) {
		this.type = type;
	}

	public Date getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
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
		String result = getClass().getSimpleName() + " ";
		if (payer != null) {
			result += payer.toShortString();
		}
		if (paymentDate != null) {
			result += ", " + new DateTime(paymentDate).toString("dd.MM.yyyy");
		}
		result += ", " + (finished != null && finished ? "finished" : "offen");
		if (comment != null && !comment.trim().isEmpty())
			result += ", " + comment;
		return result;
	}
}