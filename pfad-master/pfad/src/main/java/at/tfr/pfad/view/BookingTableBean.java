/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Stateful;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import at.tfr.pfad.BookingStatus;
import at.tfr.pfad.util.ColumnModel;

@Named
@ViewScoped
@Stateful
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class BookingTableBean extends BaseBean {

	private static int cnt = 0;
	private String selectionMode = "multiple";
	private List<ColumnModel> columns = new ArrayList<>();
	protected final Map<String,String> jaNeinAnz = new HashMap<>();

	@Inject
	private transient BookingDataModel bookingDataModel;

	public BookingTableBean() {
		jaNeinAnz.put("Ja", "true");
		jaNeinAnz.put("Nein", "false");
		jaNeinAnz.put("Anz", "anz");
		jaNeinAnz.put("keine", "none");
	}

	@PostConstruct
	public void postConstruct() {
		columns.add(new ColumnModel("id", "ID", 0).search(false));
		columns.add(new ColumnModel("member", "Mitglied", 1, true).minLength(2));
		columns.add(new ColumnModel("strasse", "Strasse", 2).minLength(2)); //.items(memberRepo.findDistinctStrasse()));
		columns.add(new ColumnModel("ort", "Ort", 3).items(memberRepo.findDistinctOrt()));
		columns.add(new ColumnModel("activity", "Aktivität", 4)); //.items(activityRepo.findDistinctName()));
		columns.add(new ColumnModel("activityFinished", "Beendet", 4).items(trueFalse).filter("false")
				.headerStyle("border: solid 3px red;").headerStyleValue("false")); //.items(activityRepo.findDistinctName()));
		columns.add(new ColumnModel("squadName", "Trupp", 5).items(squadRepo.findDistinctName()));
		columns.add(new ColumnModel("status", "Status", 6).items(BookingStatus.values()));
		columns.add(new ColumnModel("payed", "Bezahlt", 7).items(jaNeinAnz));
		columns.add(new ColumnModel("comment", "Bemerkung", 8));
		bookingDataModel.setColumns(columns);
	}

	public List<ColumnModel> getColumns() {
		return columns;
	}

	public BookingDataModel getDataModel() {
		return bookingDataModel;
	}

	/**
	 * @return the selectionMode
	 */
	public String getSelectionMode() {
		return selectionMode;
	}

	/**
	 * @param selectionMode the selectionMode to set
	 */
	public void setSelectionMode(final String selectionMode) {
		this.selectionMode = selectionMode;
	}

	public int getCnt() {
		return ++cnt;
	}
	
	@Override
	public boolean isUpdateAllowed() {
		return sessionBean.isTruppsAllowed();
	}
	
	@Override
	public void retrieve() {
	}
}
