package at.tfr.pfad.view;

import java.io.Serializable;
import java.security.Principal;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import at.tfr.pfad.dao.ConfigurationRepository;
import at.tfr.pfad.model.Configuration;

@Named
@SessionScoped
public class SessionBean implements Serializable {

	private Principal user;
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
		user = memberBean.getSessionContext().getCallerPrincipal();
		config = configRepo.findAll();
		registrationEndDate = configurationBean.getRegistrationEndDate();
	}

	public List<Configuration> getConfig() {
		return config;
	}

	public void setConfig(List<Configuration> config) {
		this.config = config;
	}

	public Principal getUser() {
		return user;
	}

	public void setUser(Principal user) {
		this.user = user;
	}

	public Date getRegistrationEndDate() {
		return registrationEndDate;
	}

	public void setRegistrationEndDate(Date registrationEndDate) {
		this.registrationEndDate = registrationEndDate;
	}
}
