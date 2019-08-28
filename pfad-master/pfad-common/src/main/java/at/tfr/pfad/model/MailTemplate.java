package at.tfr.pfad.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import at.tfr.pfad.dao.AuditListener;

@Audited(withModifiedFlag = true)
@Entity
@EntityListeners({ AuditListener.class })
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class MailTemplate extends BaseEntity implements Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mailTemplate_seq")
	@SequenceGenerator(name = "mailTemplate_seq", sequenceName = "mailTemplate_seq", allocationSize = 1, initialValue = 1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@Column(length=64)
	private String name;
	@Column(length=256)
	private String subject;
	@Column(length=40960)
	private String text;
	@Column(length=4096)
	private String query;
	@Column(length=64)
	private String owner;
	@Column
	private Boolean saveText;
	
	protected Date changed;
	protected Date created;
	protected String changedBy;
	protected String createdBy;

	@NotAudited
	@OneToMany(mappedBy="template")
	private List<MailMessage> messages;

	@Override
	public Long getId() {
		return id;
	}

	public String getIdStr() {
		return id != null ? id.toString() : null;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getOwner() {
		return owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public List<MailMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<MailMessage> messages) {
		this.messages = messages;
	}
	
	public Boolean getSaveText() {
		return saveText;
	}
	
	public void setSaveText(Boolean saveText) {
		this.saveText = saveText;
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
		return "MailTemplate [id=" + id + ", name=" + name + ", text=" + StringUtils.abbreviate(text, 50) + ", query=" + StringUtils.abbreviate(query, 50) + "]";
	}

}
