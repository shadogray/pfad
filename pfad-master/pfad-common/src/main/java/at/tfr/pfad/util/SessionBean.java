package at.tfr.pfad.util;

import java.io.Serializable;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import at.tfr.pfad.Role;
import at.tfr.pfad.dao.ConfigurationRepository;
import at.tfr.pfad.dao.SquadRepository;
import at.tfr.pfad.model.Configuration;
import at.tfr.pfad.model.Squad;

@Named
@SessionScoped
public class SessionBean implements Serializable {
	
	private Logger log = Logger.getLogger(getClass());
	private List<Configuration> config;
	private Date registrationEndDate;
	private boolean logActive;
	@Inject
	protected UserSession userSession;
	@Inject
	private ConfigurationRepository configRepo;
	@Inject
	private SquadRepository squadRepo;
	private Squad squad;
	private boolean squadTested;
	
	@PostConstruct
	public void init() {
		config = configRepo.findAll();
		if (!(isAdmin() || isGruppe())) {
			config = config.stream()
				.filter(c -> c.getRole() == null || Role.none.equals(c.getRole()) || userSession.isCallerInRole(c.getRole().name()))
				.filter(c -> StringUtils.isEmpty(c.getOwners()) || 
						c.getOwners().toLowerCase().contains(userSession.getCallerPrincipal().getName()))
				.collect(Collectors.toList());
		}		
		registrationEndDate = getRegistrationEndDate();
	}

	public List<Configuration> getConfig() {
		return config;
	}

	public void setConfig(List<Configuration> config) {
		this.config = config;
	}

	public Principal getUser() {
		return userSession.getCallerPrincipal();
	}
	
	public boolean isAnonymous() {
		return "anonymous".equals(userSession.getCallerPrincipal());
	}

	public boolean isAdmin() {
		return userSession.isCallerInRole(Role.admin.name());
	}
	
	public boolean isGruppe() {
		return userSession.isCallerInRole(Role.gruppe.name());
	}

	public boolean isLeiter() {
		return userSession.isCallerInRole(Role.leiter.name());
	}
	
	public boolean isRegistrierung() {
		return userSession.isCallerInRole(Role.registrierung.name());
	}
	
	public boolean isAnmeldung() {
		return userSession.isCallerInRole(Role.anmeldung.name());
	}
	
	public boolean isTrainer() {
		return userSession.isCallerInRole(Role.training.name());
	}
	
	public Squad isResponsibleFor() {
		if (isLeiter()) {
			String name = userSession.getCallerPrincipal().getName();
			Optional<Squad> sOpt = squadRepo.findAll().stream().filter(s-> name.equalsIgnoreCase(s.getLogin())).findAny();
			if (sOpt.isPresent()) {
				return sOpt.get();
			}
		}
		return null;
	}
	
	public boolean isTruppsAllowed() {
		return isAdmin() || isGruppe() || isLeiter();
	}
	
	public boolean isRegistrierungAllowed() {
		return isAdmin() || isGruppe() || isRegistrierung() || isAnmeldung();
	}
	
	public boolean isTrainingAllowed() {
		return isAdmin() || isGruppe() || isTrainer();
	}
	
	public boolean isRegActionsAllowed() {
		return isAdmin() || isGruppe();
	}
	
	public Squad getSquad() {
		if (!squadTested) {
			squadTested = true;
			squad = isResponsibleFor();
		}
		return squad;
	}

	public boolean isKassier() {
		return userSession.isCallerInRole(Role.kassier.name());
	}

	public boolean isVorstand() {
		return userSession.isCallerInRole(Role.vorstand.name());
	}

	public UserSession getUserSession() {
		return userSession;
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
			}
		}
		return registrationEndDate;
	}

	public boolean isLogActive() {
		return logActive;
	}
	
	public void setLogActive(boolean logActive) {
		this.logActive = logActive;
	}
	
	public void toggleLog() {
		logActive = !logActive;
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
