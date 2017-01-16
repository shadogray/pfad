/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import at.tfr.pfad.model.PaymentDataModel;

@Named
@ViewScoped
public class PaymentTableBean implements Serializable {

	private static int cnt = 0;
	private String selectionMode = "multiple";
	private List<SortMeta> sortOrders = new ArrayList<>();
	private Map<String, String> columnHeaders = new HashMap<String, String>();
	private Map<String, String> filterValues = new HashMap<String, String>();
	private String sortProperty;

	@Inject
	private PaymentDataModel paymentDataModel;

	public PaymentTableBean() {
//		sortOrders.put("id", SortOrder.UNSORTED);
//		sortOrders.put("payer", SortOrder.UNSORTED);
//		sortOrders.put("amount", SortOrder.UNSORTED);
//		sortOrders.put("member", SortOrder.UNSORTED);
//		sortOrders.put("squad", SortOrder.UNSORTED);
//		sortOrders.put("activity", SortOrder.UNSORTED);
//		sortOrders.put("aconto", SortOrder.UNSORTED);
//		sortOrders.put("finished", SortOrder.UNSORTED);
//		sortOrders.put("comment", SortOrder.UNSORTED);
	}

	@PostConstruct
	public void postConstruct() {
		columnHeaders.put("id", "ID");
		columnHeaders.put("payer", "Zahler");
		columnHeaders.put("amount", "Betrag");
		columnHeaders.put("member", "Mitglied");
		columnHeaders.put("squad", "Trupp");
		columnHeaders.put("activity", "Aktivität");
		columnHeaders.put("aconto", "Akto");
		columnHeaders.put("finished", "Erledigt");
		columnHeaders.put("comment", "Bemerkung");
	}

	public Map<String, String> getColumnHeaders() {
		return columnHeaders;
	}

	public List<SortMeta> getSortOrders() {
		return sortOrders;
	}

	public Map<String, String> getFilterValues() {
		return filterValues;
	}

	public String getSortProperty() {
		return sortProperty;
	}

	public void setSortProperty(final String sortPropety) {
		sortProperty = sortPropety;
	}

	public void toggleSort() {
		for (final SortMeta entry : sortOrders) {
			SortOrder newOrder;

			if (entry.getSortField().equals(sortProperty)) {
				if (entry.getSortOrder() == SortOrder.ASCENDING) {
					newOrder = SortOrder.DESCENDING;
				} else {
					newOrder = SortOrder.ASCENDING;
				}
			} else {
				newOrder = SortOrder.UNSORTED;
			}

			entry.setSortOrder(newOrder);
		}
	}

	public PaymentDataModel getDataModel() {
		return paymentDataModel;
	}

	/**
	 * @return the selectionMode
	 */
	public String getSelectionMode() {
		return selectionMode;
	}

	/**
	 * @param selectionMode
	 *            the selectionMode to set
	 */
	public void setSelectionMode(final String selectionMode) {
		this.selectionMode = selectionMode;
	}

	public int getCnt() {
		return ++cnt;
	}
}
