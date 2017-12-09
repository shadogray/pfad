package at.tfr.pfad.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import at.tfr.pfad.dao.ConfigurationRepository;
import at.tfr.pfad.dao.MailMessageRepository;
import at.tfr.pfad.dao.MailTemplateRepository;
import at.tfr.pfad.model.Configuration;
import at.tfr.pfad.model.MailMessage;
import at.tfr.pfad.model.MailTemplate;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.util.QueryExecutor;
import at.tfr.pfad.util.TemplateUtils;

@Named
@ViewScoped
@Stateful
public class MailerBean extends BaseBean {

	private Logger log = Logger.getLogger(getClass());

	@Inject
	private ConfigurationRepository configRepo;
	@Inject
	private TemplateUtils templateUtils;
	@Inject
	private QueryExecutor queryExec;
	@Inject
	private MailTemplateRepository templateRepo;
	@Inject
	private MailMessageRepository messageRepo;
	@Inject
	private MailTemplateBean mailTemplateBean;

	private List<Map<String, Object>> values;
	private Configuration aliasConf;
	private Configuration ccConf;
	private Configuration bccConf;
	private String from;
	private String username;
	private String password;
	private MailTemplate mailTemplate = new MailTemplate();
	private List<MailMessage> mailMessages;

	public enum MailProps {
		mail_transport_protocol, mail_smtp_starttls_enable, mail_smtp_auth, mail_smtp_host, mail_smtp_port, mail_smtps_auth, mail_smtps_host, mail_smtps_port, mail_smtp_ssl_enable, mail_smtp_socketFactory_class, mail_smtp_socketFactory_port
	}

	@PostConstruct
	public void init() {
		username = configRepo.getValue("mail_username", null);
		from = configRepo.getValue("mail_from", null);
		Configuration pwdConf = configRepo.findOptionalByCkey("mail_password");
		password = pwdConf != null ? pwdConf.getCvalueIntern() : null;
		aliasConf = configRepo.findOptionalByCkey("mail_alias");
		ccConf = configRepo.findOptionalByCkey("mail_cc");
		bccConf = configRepo.findOptionalByCkey("mail_bcc");
	}

	public void executeQuery() {
		mailMessages = new ArrayList<>();
		try {
			values = queryExec.execute(mailTemplate.getQuery(), false);
			for (Map<String, Object> value : values) {
				MailMessage msg = new MailMessage();
				msg.setValues(value);
				msg.setTemplate(mailTemplate);
				msg.setText(templateUtils.replace(mailTemplate.getText(), value));
				msg.setReceiver(templateUtils.replace("${to}", value));
				msg.setSubject(templateUtils.replace(mailTemplate.getSubject(), msg.getValues()));
				msg.setMember(value.entrySet().stream().filter(e -> e.getValue() instanceof Member)
						.map(e -> (Member) e.getValue()).findFirst().orElse(null));
				mailMessages.add(msg);
			}
		} catch (Exception e) {
			log.warn("Cannot execute: " + mailTemplate + e, e);
			error("Cannot execute: " + mailTemplate + e);
		}
	}

	public void saveTemplate() {
		try {
			mailTemplate = templateRepo.saveAndFlush(mailTemplate);
		} catch (Exception e) {
			log.warn("Cannot save: " + mailTemplate + " : " + e, e);
			error("Cannot save: " + e);
		}
	}

	public void sendMessages() {
		try {

			mailTemplate = templateRepo.saveAndFlush(mailTemplate);

			Properties props = new Properties();
			if (log.isDebugEnabled())
				props.put("mail.debug", "true");
			for (MailProps mp : MailProps.values()) {
				Configuration conf = configRepo.findOptionalByCkey(mp.name());
				if (conf != null) {
					props.put(mp.name().replaceAll("_", "."), conf.getCvalue());
				}
			}

			Session session = Session.getInstance(props, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
			InternetAddress sender = new InternetAddress(from);
			if (aliasConf != null) {
				sender.setPersonal(aliasConf.getCvalue());
			}

			for (MailMessage msg : mailMessages) {
				try {
					if (StringUtils.isBlank(msg.getReceiver()) || StringUtils.isBlank(msg.getText())
							|| StringUtils.isBlank(msg.getSubject())) {
						warn("invalid message: " + msg);
						continue;
					}
					MimeMessage mail = new MimeMessage(session);
					mail.setFrom(sender);
					mail.setSubject(msg.getSubject());
					mail.setContent(msg.getText(), "text/html; charset=utf-8");
					RecipientType to = RecipientType.TO;
					addAddresses(mail, msg.getReceiver(), to);
					if (ccConf != null) {
						addAddresses(mail, ccConf.getCvalue(), RecipientType.CC);
					}
					if (bccConf != null) {
						addAddresses(mail, bccConf.getCvalue(), RecipientType.BCC);
					}

					msg.setTemplate(mailTemplate);
					msg.setCreatedBy(sessionBean.getUser().getName());
					msg = messageRepo.saveAndFlush(msg);

					try {
						Transport.send(mail);

						msg.setCreated(new Date());
						messageRepo.saveAndFlush(msg);
					} catch (Exception e) {
						try {
							messageRepo.removeAndFlush(msg);
						} catch (Exception er) {
							log.warn("cannot remove unsent: " + msg + " : " + er);
						}
						throw e; // dont forget to rethrow!!
					}
				} catch (MessagingException me) {
					log.info("send failed: " + me + " msg: " + msg, me);
					warn("send failed: " + me + " msg: " + msg);
					break;
				}
			}

		} catch (Exception e) {
			log.warn("Cannot send messages: " + mailTemplate + " : " + e, e);
			error("cannot send messages: " + e);
		}
	}

	private void addAddresses(MimeMessage mail, String receivers, RecipientType type)
			throws MessagingException, AddressException {
		Arrays.stream(receivers.split("[,;]")).forEach(r -> {
			try {
				if (StringUtils.isNotBlank(r)) {
					mail.addRecipient(type, new InternetAddress(r));
				}
			} catch (Exception e) {
				log.info("cannot convert to Address: " + r + " : " + e);
			}
		});
	}

	@Override
	public boolean isUpdateAllowed() {
		return isVorstand() || isAdmin();
	}

	public List<Map<String, Object>> getValues() {
		return values;
	}

	public List<String> getValueKeys() {
		List<String> keys = new ArrayList<>();
		if (values != null && !values.isEmpty()) {
			keys.addAll(values.get(0).keySet());
		}
		return keys;
	}

	public String getUsername() {
		return username + ":" + (password != null ? "*******" : "");
	}

	public String getFrom() {
		return from;
	}

	public Configuration getCcConf() {
		return ccConf;
	}

	public Configuration getBccConf() {
		return bccConf;
	}

	public Configuration getAliasConf() {
		return aliasConf;
	}

	public List<MailMessage> getMailMessages() {
		return mailMessages;
	}

	public MailTemplate getMailTemplate() {
		return mailTemplate;
	}

	public void setMailTemplate(MailTemplate mailTemplate) {
		if (mailTemplate != null && mailTemplate.getId() != null
				&& !mailTemplate.getId().equals(this.mailTemplate.getId())) {
			this.mailTemplate = mailTemplate;
		}
	}

	public Converter getConverter() {
		return new Converter() {
			Converter converter = mailTemplateBean.getConverter();

			@Override
			public String getAsString(FacesContext context, UIComponent component, Object value) {
				return converter.getAsString(context, component, value);
			}

			@Override
			public Object getAsObject(FacesContext context, UIComponent component, String value) {
				try {
					MailTemplate tmp = (MailTemplate) converter.getAsObject(context, component, value);
					return (mailTemplate != null && tmp.getId() == mailTemplate.getId()) ? mailTemplate : tmp;
				} catch (Exception e) {
				}
				if (mailTemplate == null)
					mailTemplate = new MailTemplate();
				return mailTemplate;
			}
		};
	}
}
