package at.tfr.pfad.util;

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
        evt.handledAndContinue();
    }
}