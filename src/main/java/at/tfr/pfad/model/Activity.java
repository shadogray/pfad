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
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.Audited;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import at.tfr.pfad.ActivityStatus;
import at.tfr.pfad.ActivityType;
import at.tfr.pfad.dao.AuditListener;

@Audited(withModifiedFlag = true)
@Entity
@EntityListeners({AuditListener.class})
public class Activity implements PrimaryKeyHolder, Auditable, Serializable {

	private static DateTimeFormatter format = DateTimeFormat.forPattern("dd.MM.yyyy");
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@OneToMany(mappedBy = "activity")
	private Set<Booking> bookings = new HashSet<Booking>();

	@Column
	@Temporal(TemporalType.DATE)
	private Date start;

	@Column
	@Temporal(TemporalType.DATE)
	private Date end;

	@Column
	private String name;

	@Column(nullable=false, columnDefinition="varchar2(16) default 'Membership' not null")
	@Enumerated(EnumType.STRING)
	private ActivityType type;
	
	@Column(nullable=false, columnDefinition="varchar2(16) default 'planned' not null")
	@Enumerated(EnumType.STRING)
	private ActivityStatus status;
	
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
		if (!(obj instanceof Activity)) {
			return false;
		}
		Activity other = (Activity) obj;
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

	public Set<Booking> getBookings() {
		return this.bookings;
	}

	public void setBookings(final Set<Booking> bookings) {
		this.bookings = bookings;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}
	
	public String getStartString() {
		return start != null ? new DateTime(start).toString(format) : "";
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public String getEndString() {
		return end != null ? new DateTime(end).toString(format) : "";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ActivityType getType() {
		return type;
	}

	public void setType(ActivityType type) {
		this.type = type;
	}

	public ActivityStatus getStatus() {
		return status;
	}

	public void setStatus(ActivityStatus status) {
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

	@Override
	public String toString() {
		String result = (StringUtils.isNotBlank(name) ? type + ":" + name : getClass().getSimpleName());
		if (start != null) {
			result += ", " + new DateTime(start).toString(format);
		}
		return result;
	}
}