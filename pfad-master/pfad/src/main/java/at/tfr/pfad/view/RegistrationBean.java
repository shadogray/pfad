package at.tfr.pfad.view;

import java.io.Serializable;
import java.util.List;

import javax.faces.model.ListDataModel;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import at.tfr.pfad.dao.RegistrationRepository;
import at.tfr.pfad.model.Registration;

@Named
@ViewScoped
public class RegistrationBean implements Serializable {

	@Inject
	private RegistrationRepository regRepo;
	private Registration example;
	private List<Registration> dataModel;
	
	public void paginate() {
		dataModel = regRepo.fetchAll();
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
}
