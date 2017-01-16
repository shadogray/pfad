package at.tfr.pfad;

import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;

@ApplicationScoped
public class UiProvider {


	@Produces
	public Locale getLocale() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		if (ctx != null) {
			return ctx.getViewRoot().getLocale();
		}
		return Locale.GERMAN;
	}
	
}
