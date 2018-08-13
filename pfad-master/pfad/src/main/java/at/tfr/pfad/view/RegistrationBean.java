package at.tfr.pfad.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.ListDataModel;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ValidationException;

import org.jboss.logging.Logger;
import org.omnifaces.util.Messages;
import org.primefaces.event.RowEditEvent;
import org.richfaces.component.UIAccordion;

import at.tfr.pfad.RegistrationStatus;
import at.tfr.pfad.dao.RegistrationRepository;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Registration;

@Named
@ViewScoped
@Stateful
public class RegistrationBean extends BaseBean {

	private Logger log = Logger.getLogger(getClass());
	
	@Inject
	private RegistrationRepository regRepo;
	private Registration example = new Registration();
	private RegistrationStatus[] filterStati;
	private List<Integer> distinctGebJahr = new ArrayList<>();
	private List<Integer> distinctSchoolEntry = new ArrayList<>();
	private List<Registration> dataModel;
	private List<Registration> filteredDataModel = new ArrayList<>();
	private Long id;
	private Registration registration;
	private boolean all;

	@PostConstruct
	public void init() {
		distinctGebJahr = regRepo.findDistinctGebJahr();
		distinctSchoolEntry = regRepo.findDistinctSchoolEntry();
		filterStati = new RegistrationStatus[] 
				{ RegistrationStatus.ZusageGes, RegistrationStatus.AbsageGes}; 
		paginate();
	}
	
	
	public void retrieve() {
		if ((sessionBean.isAdmin() || sessionBean.isRegistrierung() || sessionBean.isAnmeldung()) && id == null) {
			registration = new Registration();
			registration.setAktiv(true);
			registration.setStatus(RegistrationStatus.Erstellt);
		} else {
			registration = regRepo.findBy(id);
		}
	}

	public void paginate() {
		// Belli: sollen alle sehen können
		dataModel = regRepo.queryBy(example, filterStati, (all ? null : Boolean.TRUE), (all ? null : Boolean.FALSE));
	}

	public void update() {
		if (isUpdateAllowed()) {
			registration = updateRegistration(registration);
			id = registration.getId();
			info("Anmeldung aktualisert..");
		} else {
			error("Keine Berechtigung zur Änderung für Benutzer: " + sessionBean.getUser() + "!");
		}
	}

	public Registration updateRegistration(Registration reg) {
		return regRepo.saveAndFlush(reg);
	}

	public void onRowEdit(RowEditEvent event) {
		if (!isUpdateAllowed() || !(event.getObject() instanceof Registration)) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Anmeldung NICHT geändert!!", "Keine Berechtigung");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} else {
			Registration extReg = (Registration) event.getObject();
			Registration reg = regRepo.findBy(extReg.getId());
			reg.setName(extReg.getName());
			reg.setVorname(extReg.getVorname());
			reg.setGeschlecht(extReg.getGeschlecht());
			reg.setGebJahr(extReg.getGebJahr());
			reg.setGebMonat(extReg.getGebMonat());
			reg.setGebTag(extReg.getGebTag());
			reg.setSchoolEntry(extReg.getSchoolEntry());
			reg.setEmail(extReg.getEmail());
			reg.setTelefon(extReg.getTelefon());

			reg.setStrasse(extReg.getStrasse());
			reg.setOrt(extReg.getOrt());
			reg.setPLZ(extReg.getPLZ());
			reg.setParentName(extReg.getParentName());
			reg.setParentVorname(extReg.getParentVorname());
			reg.setStatus(extReg.getStatus());
			reg.setAktiv(extReg.isAktiv());
			reg.setStorno(extReg.isStorno());
			
			updateRegistration(reg);
			info("Anmeldung geändert: ", reg.toFullString());
		}
	}

	public void onRowCancel(RowEditEvent event) {
		info("Änderung Abgebrochen", "" + event.getObject());
	}
	
	public void convertToMember() {
		registration = regRepo.findBy(id);
		try {
			if (registration.getMember() != null) {
				throw new ValidationException("Conversion already done: " + registration.getMember());
			}
			
			Member parent = registration.getParent();
			if (parent == null) {
				List<Member> members = memberRepo.findByNameAndVornameAndStrasseAndOrt(registration.getParentName(), 
						registration.getParentVorname(), registration.getStrasse(), registration.getOrt());
				if (members.size() == 1) {
					parent = members.get(0);
				} else {
					parent = new Member();
					parent.setAktiv(false);
					parent.setName(registration.getParentName());
					parent.setVorname(registration.getParentVorname());
					parent.setStrasse(registration.getStrasse());
					parent.setPLZ(registration.getPLZ());
					parent.setOrt(registration.getOrt());
					parent.setEmail(registration.getEmail());
					parent.setTelefon(registration.getTelefon());
					parent = memberRepo.saveAndFlush(parent);
				}
			}
			
			Member member = new Member();
			member.setAnrede(registration.getAnrede());
			member.setName(registration.getName());
			member.setVorname(registration.getVorname());
			member.setGeschlecht(registration.getGeschlecht());
			
			member.setGebJahr(registration.getGebJahr());
			member.setGebMonat(registration.getGebMonat());
			member.setGebTag(registration.getGebTag());
			
			member.setStrasse(registration.getStrasse());
			member.setPLZ(registration.getPLZ());
			member.setOrt(registration.getOrt());
			member.setEmail(registration.getEmail());
			member.setTelefon(registration.getTelefon());
			
			member.setAktiv(true);
			member.setTrail(true);
			
			member = memberRepo.saveAndFlush(member);
			
			parent.getSiblings().add(member);
			member.addParent(parent);
			
			memberRepo.flush();
			
			registration.setParent(parent);
			registration.setMember(member);
			registration.setAktiv(false);
			regRepo.saveAndFlush(registration);
			
			info("In Mitglied umgewandelt: " + member);
			
		} catch (Exception e) {
			log.info("Cannot convert: " + registration + " : " + e, e);
			error("Anmeldung NICHT geändert!! Fehler: " + e.getLocalizedMessage());
		}
	}

	public boolean isUpdateAllowed() {
		return sessionBean.isAdmin() || sessionBean.isGruppe() || sessionBean.isRegistrierung()
				|| sessionBean.isAnmeldung();
	}

	public ListDataModel<Registration> getDataModel() {
		return new ListDataModel<Registration>(dataModel);
	}

	public List<Registration> getFilteredDataModel() {
		return filteredDataModel;
	}
	
	public void setFilteredDataModel(List<Registration> filteredDataModel) {
		this.filteredDataModel = filteredDataModel;
	}

	public Registration getExample() {
		return example;
	}

	public void setExample(Registration example) {
		this.example = example;
	}
	
	public RegistrationStatus[] getFilterStati() {
		return filterStati;
	}
	
	public void setFilterStati(RegistrationStatus[] filterStati) {
		this.filterStati = filterStati;
	}

	public List<Integer> getDistinctGebJahr() {
		return distinctGebJahr;
	}
	
	public List<Integer> getDistinctSchoolEntry() {
		return distinctSchoolEntry;
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
	
	public List<RegistrationStatus> getStati() {
		return Arrays.asList(RegistrationStatus.values());
	}
}
