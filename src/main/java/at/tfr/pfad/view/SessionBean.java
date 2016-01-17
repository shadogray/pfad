package at.tfr.pfad.view;

import java.io.Serializable;
import java.security.Identity;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
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
public class SessionBean implements Serializable {
	
	private Logger log = Logger.getLogger(getClass());
	private List<Configuration> config;
	private Date registrationEndDate;
	@Inject 
	private MemberBean memberBean;
	@Inject
	private ConfigurationRepository configRepo;
	@Inject
	private ConfigurationBean configurationBean;
	
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
		return memberBean.getSessionContext().getCallerPrincipal();
	}

	public boolean isAdmin() {
		return configurationBean.isAdmin();
	}
	
	public boolean isGruppe() {
		return configurationBean.isGruppe();
	}

	public boolean isLeiter() {
		return configurationBean.isLeiter();
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
