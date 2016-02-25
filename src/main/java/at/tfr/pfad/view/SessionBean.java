package at.tfr.pfad.view;

import java.io.Serializable;
import java.security.Principal;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import at.tfr.pfad.dao.ConfigurationRepository;
import at.tfr.pfad.model.Configuration;

@Named
@SessionScoped
@Stateful
public class SessionBean implements Serializable {
	
	private Logger log = Logger.getLogger(getClass());
	private List<Configuration> config;
	private Date registrationEndDate;
	@Resource
	protected SessionContext sessionContext;
	@Inject
	private ConfigurationRepository configRepo;
	
	@PostConstruct
	public void init() {
		config = configRepo.findAll();
		registrationEndDate = getRegistrationEndDate();
	}

	public List<Configuration> getConfig() {
		return config;
	}

	public void setConfig(List<Configuration> config) {
		this.config = config;
	}

	public Principal getUser() {
		return sessionContext.getCallerPrincipal();
	}

	public boolean isAdmin() {
		return sessionContext.isCallerInRole(Role.admin.name());
	}
	
	public boolean isGruppe() {
		return sessionContext.isCallerInRole(Role.gruppe.name());
	}

	public boolean isLeiter() {
		return sessionContext.isCallerInRole(Role.leiter.name());
	}

	public boolean isKassier() {
		return sessionContext.isCallerInRole(Role.kassier.name());
	}

	public boolean isVorstand() {
		return sessionContext.isCallerInRole(Role.vorstand.name());
	}

	public SessionContext getSessionContext() {
		return sessionContext;
	}
	
	public void setRegistrationEndDate(Date registrationEndDate) {
		this.registrationEndDate = registrationEndDate;
	}

	public Date getRegistrationEndDate() {
		if (registrationEndDate == null) {
			try {
				String value = configRepo.getValue(Configuration.REGEND_KEY, null);
				if (StringUtils.isNotBlank(value)) {
					registrationEndDate = DateTime.parse(value, DateTimeFormat.forPattern("dd.MM.yyyy HH:mm")).toDate();
				}
			} catch (Exception e) {
				log.info("cannot parse Date: " + e, e);
				registrationEndDate = new Date();
			}
		}
		return registrationEndDate;
	}

	class PfadPrincipal implements Principal {
		private String name = "anonymous";
		public PfadPrincipal(String name) {
			this.name = name;
		}
		@Override
		public String getName() {
			return name;
		}
	}
}
