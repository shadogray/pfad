package at.tfr.pfad.processing;

import at.tfr.pfad.model.Activity;

public class ProcessData {

	private Activity activity;
	private Double[] amountGrades = new Double[] {70D,120D};
	private Double[] accontoGrades = new Double[] {};
	private boolean createPayment;
	
	public ProcessData() {
	}

	public ProcessData(Activity activity) {
		this.activity = activity;
	}

	public ProcessData(Activity activity, Double[] amountGrades) {
		this.activity = activity;
		this.amountGrades = amountGrades;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Double[] getAmountGrades() {
		return amountGrades;
	}

	public void setAmountGrades(Double[] amountGrades) {
		this.amountGrades = amountGrades;
	}
	
	public Double[] getAccontoGrades() {
		return accontoGrades;
	}

	public void setAccontoGrades(Double[] accontoGrades) {
		this.accontoGrades = accontoGrades;
	}

	public boolean isCreatePayment() {
		return createPayment;
	}

	public void setCreatePayment(boolean createPayment) {
		this.createPayment = createPayment;
	}
}
