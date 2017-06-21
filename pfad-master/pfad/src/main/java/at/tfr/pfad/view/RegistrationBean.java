package at.tfr.pfad.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateful;
import javax.faces.model.ListDataModel;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import at.tfr.pfad.dao.RegistrationRepository;
import at.tfr.pfad.model.Registration;
import at.tfr.pfad.view.SessionBean;

@Named
@ViewScoped
@Stateful
public class RegistrationBean implements Serializable {

	@Inject
	private SessionBean sessionBean;
	@Inject
	private RegistrationRepository regRepo;
	private Registration example;
	private List<Registration> dataModel;
	private Long id;
	private Registration registration;
	private boolean all;

	public void retrieve() {
		if (!isUpdateAllowed()) {
			registration = new Registration();
		} else {
			registration = regRepo.findBy(id);
		}
	}

	public void paginate() {
		if (!isUpdateAllowed()) {
			dataModel = new ArrayList<>();
		} else {
			dataModel = all ? regRepo.fetchAll() : regRepo.fetchAll().stream().filter(r -> r.isAktiv()).collect(Collectors.toList());
		}
	}
	
	public void update() {
		if (isUpdateAllowed()) {
			regRepo.save(registration);
		}
	}

	public boolean isUpdateAllowed() {
		return sessionBean.isAdmin() || sessionBean.isGruppe() || sessionBean.isRegistrierung() || sessionBean.isAnmeldung();
	}
	
	public javax.faces.model.DataModel<Registration> getDataModel() {
		return new ListDataModel<>(dataModel);
	}

	public Registration getExample() {
		return example;
	}

	public void setExample(Registration example) {
		this.example = example;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Registration getRegistration() {
		return registration;
	}

	public void setRegistration(Registration registration) {
		this.registration = registration;
	}

	public boolean isAll() {
		return all;
	}
	
	public void setAll(boolean all) {
		this.all = all;
	}
}
