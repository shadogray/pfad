package at.tfr.pfad.view;

import java.util.Date;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

import org.jboss.logging.Logger;
import org.primefaces.model.UploadedFile;

import at.tfr.pfad.dao.MailMessageRepository;
import at.tfr.pfad.model.MailMessage;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class MailSender {

	private Logger log = Logger.getLogger(getClass());
	
	@Inject
	private MailMessageRepository messageRepo;

	public MailMessage sendMail(Message mail, MailMessage msg) throws MessagingException {
		
		msg = messageRepo.saveAndFlush(msg);
		try {

			Transport.send(mail);
			
			msg.setCreated(new Date());
			return messageRepo.saveAndFlushAndRefresh(msg);
			
		} catch (MessagingException e) {
			log.warn("cannot send: " + msg + " : " + e);
			messageRepo.removeAndFlush(msg);
			throw e;
		}
	}
	
}
