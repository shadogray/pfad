package at.tfr.pfad.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateful;
import javax.enterprise.event.Reception;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.ListDataModel;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ValidationException;

import org.jboss.logging.Logger;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.RowEditEvent;

import at.tfr.pfad.RegistrationStatus;
import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.dao.RegistrationRepository;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.model.Registration;

@Named
@ViewScoped
@Stateful
public class RegistrationBean implements Serializable {

	private Logger log = Logger.getLogger(getClass());
	
	@Inject
	private SessionBean sessionBean;
	@Inject
	private RegistrationRepository regRepo;
	@Inject
	private MemberRepository memberRepo;
	private Registration example;
	private List<Registration> dataModel;
	private Long id;
	private Registration registration;
	private boolean all;

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
		if (!isUpdateAllowed()) {
			dataModel = new ArrayList<>();
		} else {
			dataModel = all ? regRepo.fetchAll()
					: regRepo.fetchAll().stream().filter(r -> r.isAktiv()).collect(Collectors.toList());
		}
	}

	public void update() {
		if (isUpdateAllowed()) {
			registration = regRepo.saveAndFlush(registration);
			id = registration.getId();
		}
	}

	public void onRowEdit(RowEditEvent event) {
		if (!isUpdateAllowed() || !(event.getObject() instanceof Registration)) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Anmeldung NICHT geändert!!", "Keine Berechtigung");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} else {
			registration = (Registration) event.getObject();
			update();
			FacesMessage msg = new FacesMessage("Anmeldung geändert: ", registration.toFullString());
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public void onRowCancel(RowEditEvent event) {
		FacesMessage msg = new FacesMessage("Änderung Abgebrochen", "" + event.getObject());
		FacesContext.getCurrentInstance().addMessage(null, msg);
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
			
			parent.getSiblings().add(member);
			member.addParent(parent);
			
			member = memberRepo.saveAndFlush(member);
			memberRepo.flush();
			
			registration.setParent(parent);
			registration.setMember(member);
			registration.setAktiv(false);
			regRepo.saveAndFlush(registration);
			
		} catch (Exception e) {
			log.info("Cannot convert: " + registration + " : " + e, e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Anmeldung NICHT geändert!!", e.getLocalizedMessage());
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public boolean isUpdateAllowed() {
		return sessionBean.isAdmin() || sessionBean.isGruppe() || sessionBean.isRegistrierung()
				|| sessionBean.isAnmeldung();
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
	
	public List<RegistrationStatus> getStati() {
		return Arrays.asList(RegistrationStatus.values());
	}
}
