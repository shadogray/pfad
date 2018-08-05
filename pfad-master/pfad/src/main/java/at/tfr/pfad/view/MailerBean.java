package at.tfr.pfad.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

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

	private Map<String, MailConfig> mailConfigs;
	private MailConfig mailConfig;
	private List<String> mailConfigKeys;
	private String mailConfigKey;
	private List<Map<String, Object>> values = Collections.emptyList();
	private MailTemplate mailTemplate = new MailTemplate();
	private List<MailMessage> mailMessages = Collections.emptyList();

	public enum MailProps {
		mail_transport_protocol, mail_smtp_starttls_enable, mail_smtp_auth, mail_smtp_host, mail_smtp_port, mail_smtps_auth, mail_smtps_host, mail_smtps_port, mail_smtp_ssl_enable, mail_smtp_socketFactory_class, mail_smtp_socketFactory_port
	}

	@PostConstruct
	public void init() {
		mailConfigs = MailConfig.generateConfigs(configRepo.findAll(), log.isDebugEnabled());
		mailConfigKey = getMailConfigKeys().stream().findFirst().orElse(null);
		setMailConfigKey(mailConfigKey);
		if (mailConfigs.isEmpty()) {
			warn("No MailConfiguration found! Cannot execute any Mails.");
		}
	}

	public void executeQuery() {
		if (mailConfig == null) {
			error("Cannot execute Template for empty MailConfiguration!");
			return;
		}
		mailMessages = new ArrayList<>();
		try {
			values = queryExec.execute(mailTemplate.getQuery(), false);
			for (Map<String, Object> value : values) {
				MailMessage msg = new MailMessage();
				msg.setValues(value);
				msg.setTemplate(mailTemplate);
				msg.setText(templateUtils.replace(mailTemplate.getText(), value));
				msg.setReceiver(templateUtils.replace("${to}", value));
				msg.setCc(templateUtils.replace("${cc}", value, mailConfig.getCcConf() != null ? mailConfig.getCcConf().getCvalue() : null));
				msg.setBcc(templateUtils.replace("${bcc}", value, mailConfig.getBccConf() != null ? mailConfig.getBccConf().getCvalue() : null));
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
		
		if (mailConfig == null) {
			error("Cannot execute Template for empty MailConfiguration!");
			return;
		}
		
		try {

			mailTemplate = templateRepo.saveAndFlush(mailTemplate);

			Session session = Session.getInstance(mailConfig.getProperties(), new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(mailConfig.getUsername(), mailConfig.getPassword());
				}
			});
			InternetAddress sender = new InternetAddress(mailConfig.getFrom());
			if (mailConfig.getAliasConf() != null) {
				sender.setPersonal(mailConfig.getAliasConf().getCvalue());
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
					if (StringUtils.isNotBlank(msg.getCc())) {
						addAddresses(mail, msg.getCc(), RecipientType.CC);
					} else if (mailConfig.getCcConf() != null) {
						msg.setCc(mailConfig.getCcConf().getCvalue());
						addAddresses(mail, msg.getCc(), RecipientType.CC);
					}
					if (StringUtils.isNotBlank(msg.getBcc())) {
						addAddresses(mail, msg.getBcc(), RecipientType.BCC);
					} else if (mailConfig.getBccConf() != null) {
						msg.setBcc(mailConfig.getBccConf().getCvalue());
						addAddresses(mail, msg.getBcc(), RecipientType.BCC);
					}

					msg.setTemplate(mailTemplate);
					msg.setSender(sender.getAddress()
							+ (StringUtils.isNotBlank(sender.getPersonal()) ? ":" + sender.getPersonal() : ""));
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

	public boolean isChangeOwnerAllowed() {
		return isVorstand() || isAdmin();
	}

	@Override
	public boolean isUpdateAllowed() {
		return isVorstand() || isAdmin() || ownerMatches(mailTemplate.getOwner(), ""+getSessionContext().getCallerPrincipal());
	}

	boolean ownerMatches(String owner, String principal) {
		if (owner == null || principal == null)
			return false;
		try {
			return principal.matches(owner);
		} catch (Exception e) {
			log.info("cannot check: " + e);
		}
		return false;
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

	public Collection<String> getMailConfigKeys() {
		return mailConfigs.keySet();
	}
	
	public String getMailConfigKey() {
		return mailConfigKey;
	}
	
	public void setMailConfigKey(String mailConfigKey) {
		this.mailConfigKey = mailConfigKey;
		if (mailConfigKey != null && mailConfigs.containsKey(mailConfigKey)) {
			mailConfig = mailConfigs.get(mailConfigKey);
		}
	}
	
	public MailConfig getMailConfig() {
		return mailConfig;
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
			values.clear();
			mailMessages.clear();
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

	public static class MailConfig {
		private final String key;
		private final String prefix;
		private Configuration aliasConf;
		private Configuration ccConf;
		private Configuration bccConf;
		private String from;
		private String username;
		private String password;
		private final Properties properties;

		public MailConfig(String key, Collection<Configuration> configs, boolean debug) {
			this.key = key;
			this.prefix = key + "_";
			this.username = getValue(configs, "mail_username");
			this.from = getValue(configs, "mail_from", null);
			this.password = getValueIntern(configs, "mail_password", null);
			this.aliasConf = getConfig(configs, "mail_alias");
			this.ccConf = getConfig(configs, "mail_cc");
			this.bccConf = getConfig(configs, "mail_bcc");

			properties = new Properties();
			if (debug)
				properties.put("mail.debug", "true");
			for (MailProps mp : MailProps.values()) {
				Configuration conf = getConfig(configs, mp.name());
				if (conf != null) {
					properties.put(mp.name().replaceAll("_", "."), conf.getCvalue());
				}
			}
		}

		public static Map<String, MailConfig> generateConfigs(Collection<Configuration> configs, boolean debug) {
			Map<String, MailConfig> mailConfigs = new HashMap<>();
			String MAIL_FX = "_mail_";
			List<String> keys = configs.stream().filter(c -> c.getCkey() != null).map(c -> c.getCkey())
					.filter(k -> k.contains(MAIL_FX)).map(k -> k.substring(0, k.indexOf(MAIL_FX))).distinct().sorted()
					.collect(Collectors.toList());
			keys.forEach(k -> {
				mailConfigs.put(k, new MailConfig(k, configs.stream().filter(c -> c.getCkey().startsWith(k + MAIL_FX))
						.collect(Collectors.toList()), debug));
			});
			return mailConfigs;
		}

		private String getValue(Collection<Configuration> configs, String valueKey) {
			return configs.stream().filter(c -> c.getCkey().startsWith(prefix + valueKey)).map(c -> c.getCvalue())
					.findFirst().orElse(null);
		}

		private String getValue(Collection<Configuration> configs, String valueKey, String defVal) {
			return configs.stream().filter(c -> c.getCkey().startsWith(prefix + valueKey)).map(c -> c.getCvalue())
					.findFirst().orElse(defVal);
		}

		private String getValueIntern(Collection<Configuration> configs, String valueKey, String defVal) {
			return configs.stream().filter(c -> c.getCkey().startsWith(prefix + valueKey)).map(c -> c.getCvalueIntern())
					.findFirst().orElse(defVal);
		}

		private Configuration getConfig(Collection<Configuration> configs, String valueKey) {
			return configs.stream().filter(c -> c.getCkey().startsWith(prefix + valueKey)).findFirst().orElse(null);
		}

		public String getKey() {
			return key;
		}

		public Configuration getAliasConf() {
			return aliasConf;
		}

		public void setAliasConf(Configuration aliasConf) {
			this.aliasConf = aliasConf;
		}

		public Configuration getCcConf() {
			return ccConf;
		}

		public void setCcConf(Configuration ccConf) {
			this.ccConf = ccConf;
		}

		public Configuration getBccConf() {
			return bccConf;
		}

		public void setBccConf(Configuration bccConf) {
			this.bccConf = bccConf;
		}

		public String getFrom() {
			return from;
		}

		public void setFrom(String from) {
			this.from = from;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
		
		public Properties getProperties() {
			return properties;
		}
	}
}
