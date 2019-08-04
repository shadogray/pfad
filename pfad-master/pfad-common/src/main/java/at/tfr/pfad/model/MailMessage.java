package at.tfr.pfad.model;


import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang3.StringUtils;

@Entity
public class MailMessage extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mailMessage_seq")
	@SequenceGenerator(name = "mailMessage_seq", sequenceName = "mailMessage_seq", allocationSize = 1, initialValue = 1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@ManyToOne
	private MailTemplate template;
	@ManyToOne
	private Member member;
	@ManyToOne
	private Registration registration;
	@Column(length=256)
	private String sender;
	@Column(length=256)
	private String receiver;
	@Column(length=256)
	private String cc;
	@Column(length=256)
	private String bcc;
	@Column(length=256)
	private String subject;
	@Column(length=4096)
	private String text;
	@Column
	private Boolean test;
	protected Date created;
	protected String createdBy;

	@Transient
	private Map<String,Object> values; 

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
	
	public MailTemplate getTemplate() {
		return template;
	}

	public void setTemplate(MailTemplate template) {
		this.template = template;
	}

	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public Registration getRegistration() {
		return registration;
	}
	
	public void setRegistration(Registration registration) {
		this.registration = registration;
	}
	
	public String getSender() {
		return sender;
	}
	
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getCc() {
		return cc;
	}
	
	public void setCc(String cc) {
		this.cc = cc;
	}
	
	public String getBcc() {
		return bcc;
	}
	
	public void setBcc(String bcc) {
		this.bcc = bcc;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public Boolean getTest() {
		return test;
	}
	
	public void setTest(Boolean test) {
		this.test = test;
	}

	public Map<String, Object> getValues() {
		return values;
	}
	
	public void setValues(Map<String, Object> values) {
		this.values = values;
	}

	public Date getCreated() {
		return created;
	}
	
	public void setCreated(Date created) {
		this.created = created;
	}
	
	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	@Override
	public String toString() {
		return "MailMessage [id=" + id + ", member=" + member + ", receiver=" + receiver + ", tpl=" + template + ", text=" + StringUtils.abbreviate(text, 50)
				+ "]";
	}
	
	
}
