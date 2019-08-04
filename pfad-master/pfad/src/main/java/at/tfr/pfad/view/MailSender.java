package at.tfr.pfad.view;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.mail.Message;
import javax.mail.Transport;

import org.jboss.logging.Logger;

import at.tfr.pfad.model.MailMessage;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class MailSender {

	private Logger log = Logger.getLogger(getClass());

	public boolean sendMail(Message mail, MailMessage msg) {
		try {
			Transport.send(mail);
			return true;
		} catch (Throwable e) {
			log.warn("cannot send: " + msg + " : " + e);
		}
		return false;
	}
	
}
