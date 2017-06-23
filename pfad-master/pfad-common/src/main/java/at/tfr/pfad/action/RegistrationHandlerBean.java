package at.tfr.pfad.action;

import java.io.IOException;
import java.util.Date;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import at.tfr.pfad.dao.RegistrationRepository;
import at.tfr.pfad.model.Registration;

@Stateless
public class RegistrationHandlerBean {

	private Logger log = Logger.getLogger(getClass());

	@Inject
	private RegistrationRepository regRepo;

	public void service(ServletRequest req) throws ServletException, IOException {
		log.info("req: remote=" + req.getRemoteAddr() + ", parms=" + req.getParameterMap().entrySet().stream()
				.map(e -> "" + e.getKey() + ":" + StringUtils.join(e.getValue(), ",")).collect(Collectors.joining()));
		Registration reg = new Registration();
		reg.setAktiv(true);
		reg.setCreated(new Date());
		reg.setCreatedBy(req.getRemoteAddr());
		req.getParameterMap().entrySet().stream()
				.forEach(e -> apply(reg, e.getKey(), StringUtils.join(e.getValue(), ",")));
		
		// Validate
		if (StringUtils.isBlank(reg.getVorname()) || StringUtils.isBlank(reg.getName())) {
			throw new ServletException("Invalid Registration: empty Name/Vorname - "+reg);
		}
		// save
		regRepo.save(reg);
		log.info("saved: " + reg);
	}

	public Registration apply(Registration reg, String param, String value) {
		if (value != null) {
			value = value.trim();
		}
		
		try {
			switch (param) {
			case "vornameKind":
				reg.setVorname(value);
				break;
			case "nachnameKind":
				reg.setName(value);
				break;
			case "geburtstag":
				reg.setGebTag(Integer.valueOf(value));
				break;
			case "geburtsmonat":
				reg.setGebMonat(Integer.valueOf(value));
				break;
			case "geburtsjahr":
				reg.setGebJahr(Integer.valueOf(value));
				break;
			case "schuleintritt":
				reg.setSchoolEntry(Integer.valueOf(value));
				break;
			case "vornameErziehungsberechtigter":
				reg.setParentVorname(value);
				break;
			case "nachnameErziehungsberechtigter":
				reg.setParentName(value);
				break;
			case "adresse":
				reg.setStrasse(value);
				break;
			case "postleitzahl":
				reg.setPLZ(value);
				break;
			case "ortschaft":
				reg.setOrt(value);
				break;
			case "telefon":
				reg.setTelefon(value);
				break;
			case "email":
				reg.setEmail(value);
				break;
			default:
				log.info("unknown param: " + param + " : " + value);
			}
		} catch (Exception e) {
			log.info("cannot convert: " + param + " : " + value + " err: " + e);
		}
		return reg;
	}
	
}
