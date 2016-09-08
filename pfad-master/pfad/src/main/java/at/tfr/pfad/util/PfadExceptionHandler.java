package at.tfr.pfad.util;

import javax.enterprise.context.NonexistentConversationException;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.deltaspike.core.api.exception.control.ExceptionHandler;
import org.apache.deltaspike.core.api.exception.control.Handles;
import org.apache.deltaspike.core.api.exception.control.event.ExceptionEvent;
import org.jboss.logging.Logger;

@ExceptionHandler
public class PfadExceptionHandler
{
	private Logger log = Logger.getLogger(getClass());
	
    void printExceptions(@Handles ExceptionEvent<Throwable> evt)
    {
        log.info("Exception:" + evt.getException().getMessage(), evt.getException());
        if (evt.getException() instanceof NonexistentConversationException
        		|| evt.getException() instanceof ViewExpiredException) {
        	try {
        		ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
				ectx.redirect(ectx.getRequestContextPath()+"/login.xhtml");
        	} catch (Exception e) {
        		log.info("cannot redirect: "+e, e);
        	}
        }
        evt.handledAndContinue();
    }
}