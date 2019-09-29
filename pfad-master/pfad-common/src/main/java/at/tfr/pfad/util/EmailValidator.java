package at.tfr.pfad.util;

import javax.enterprise.inject.Model;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.validation.ValidationException;

@Model
public class EmailValidator implements Validator {

	@Override
	public void validate(FacesContext ctx, UIComponent comp, Object value) throws ValidatorException {
		if (value instanceof String) {
			try {
				String[] addrs = ((String)value).replaceAll(";", ",").split("[, ]+");
				for (String addr : addrs) {
					if (!addr.contains("@")) 
						throw new AddressException("MailAdresse muss ein \"@\" enthalten: "+addr);
					InternetAddress.parse(value.toString(), true);
				}
			} catch (Exception e) {
				throw new ValidationException("Ungültige MailAddresse: "+value, e);
			}
		}
	}

}
