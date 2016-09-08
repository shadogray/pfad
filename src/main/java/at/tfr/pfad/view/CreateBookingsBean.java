package at.tfr.pfad.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.scope.WindowScoped;

import at.tfr.pfad.model.Activity;
import at.tfr.pfad.model.Squad;

@Named
@WindowScoped
public class CreateBookingsBean implements Serializable {

	@Inject
	private transient BookingActionBean bookingActionBean;
	
	private Activity activity;
	private List<Squad> squads = new ArrayList<>();
	private boolean withAssistants;
	private boolean fromToVisible;
	private Activity sourceActivity;
	private Activity targetActivity;
	private boolean showFinished;
	private boolean squadBookingVisible;
	private boolean allBookingVisible;

	public boolean isShowFinished() {
		return showFinished;
	}

	public void setShowFinished(boolean showFinished) {
		this.showFinished = showFinished;
	}

	public boolean isSquadBookingVisible() {
		return squadBookingVisible;
	}

	public void setSquadBookingVisible(boolean squadBookingVisible) {
		this.squadBookingVisible = squadBookingVisible;
	}

	public boolean isAllBookingVisible() {
		return allBookingVisible;
	}

	public void setAllBookingVisible(boolean allBookingVisible) {
		this.allBookingVisible = allBookingVisible;
	}


	public List<Squad> getSquads() {
		return squads;
	}

	public void setSquads(List<Squad> squads) {
		this.squads = squads;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public boolean isWithAssistants() {
		return withAssistants;
	}

	public void setWithAssistants(boolean withAssistants) {
		this.withAssistants = withAssistants;
	}

	public String createBookings() {
		return bookingActionBean.createBookings(squads, activity, withAssistants);
	}

	public String createBookingsForAllActive() {
		return bookingActionBean.createBookingsForAllActive(activity);
	}

	public String createBookingsFromSource() {
		return bookingActionBean.createBookingsFromSource(sourceActivity, targetActivity);
	}

	public Activity getSourceActivity() {
		return sourceActivity;
	}

	public void setSourceActivity(Activity sourceActivity) {
		this.sourceActivity = sourceActivity;
	}

	public Activity getTargetActivity() {
		return targetActivity;
	}

	public void setTargetActivity(Activity targetActivity) {
		this.targetActivity = targetActivity;
	}

	public boolean isFromToVisible() {
		return fromToVisible;
	}

	public void setFromToVisible(boolean fromToVisible) {
		this.fromToVisible = fromToVisible;
	}


}
