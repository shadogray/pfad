/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.richfaces.component.SortOrder;

import at.tfr.pfad.model.BookingDataModel;
import at.tfr.pfad.model.Squad;

@Named
@ViewScoped
public class BookingTableBean implements Serializable {

	    private static int cnt = 0;
	    private String selectionMode = "multiple";
	    private Map<String, SortOrder> sortOrders = new HashMap<String, SortOrder>();
	    private Map<String, String> columnHeaders = new HashMap<String, String>();
	    private Map<String, String> filterValues = new HashMap<String, String>();
	    private String sortProperty;
	    private List<Squad> squads;

	    @Inject
	    private BookingDataModel bookingDataModel;
	    
	    @Inject
	    private SquadBean squadBean;

	    public BookingTableBean() {
	    }

	    @PostConstruct
	    public void postConstruct() {
	        columnHeaders.put("id", "ID");
	        columnHeaders.put("member", "Mitglied");
	        columnHeaders.put("strasse", "Strasse");
	        columnHeaders.put("ort", "Ort");
	        columnHeaders.put("activity", "Aktivität");
	        columnHeaders.put("squadName", "Trupp");
	        columnHeaders.put("status", "Status");
	        columnHeaders.put("payed", "Bezahlt");
	        columnHeaders.put("comment", "Bemerkung");

	        sortOrders.put("id", SortOrder.unsorted);
	        sortOrders.put("member", SortOrder.unsorted);
	        sortOrders.put("strasse", SortOrder.unsorted);
	        sortOrders.put("ort", SortOrder.unsorted);
	        sortOrders.put("activity", SortOrder.unsorted);
	        sortOrders.put("squadName", SortOrder.unsorted);
	        sortOrders.put("status", SortOrder.unsorted);
	        sortOrders.put("comment", SortOrder.unsorted);
	        sortOrders.put("payed", SortOrder.unsorted);
	        
	        squads = squadBean.getAll();
	    }

	    public Map<String, String> getColumnHeaders() {
	        return columnHeaders;
	    }
	    
	    public Map<String, SortOrder> getSortOrders() {
	        return sortOrders;
	    }

	    public void setSortOrders(Map<String, SortOrder> sortOrders) {
			this.sortOrders = sortOrders;
		}
	    
	    public Map<String, String> getFilterValues() {
	        return filterValues;
	    }
	    
	    public void setFilterValues(Map<String, String> filterValues) {
			this.filterValues = filterValues;
		}

	    public String getSortProperty() {
	        return sortProperty;
	    }

	    public void setSortProperty(final String sortPropety) {
	        sortProperty = sortPropety;
	    }

	    public void toggleSort() {
	        for (final Entry<String, SortOrder> entry : sortOrders.entrySet()) {
	            SortOrder newOrder;

	            if (entry.getKey().equals(sortProperty)) {
	                if (entry.getValue() == SortOrder.ascending) {
	                    newOrder = SortOrder.descending;
	                } else {
	                    newOrder = SortOrder.ascending;
	                }
	            } else {
	                newOrder = SortOrder.unsorted;
	            }

	            entry.setValue(newOrder);
	        }
	    }

	    public BookingDataModel getDataModel() {
	        return bookingDataModel;
	    }

	    public List<Squad> getSquads() {
			return squads;
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
