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

import at.tfr.pfad.model.BookingDataModel;
import at.tfr.pfad.model.Squad;

@Named
@ViewScoped
public class BookingTableBean implements Serializable {

	    private static int cnt = 0;
	    private String selectionMode = "multiple";
	    private List<SortMeta> sortOrders = new ArrayList<>();
	    private Map<String, String> columnHeaders = new HashMap<>();
	    private Map<String, String> filterValues = new HashMap<>();
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

//	        sortOrders.add("id", SortOrder.UNSORTED);
//	        sortOrders.put("member", SortOrder.UNSORTED);
//	        sortOrders.put("strasse", SortOrder.UNSORTED);
//	        sortOrders.put("ort", SortOrder.UNSORTED);
//	        sortOrders.put("activity", SortOrder.UNSORTED);
//	        sortOrders.put("squadName", SortOrder.UNSORTED);
//	        sortOrders.put("status", SortOrder.UNSORTED);
//	        sortOrders.put("comment", SortOrder.UNSORTED);
//	        sortOrders.put("payed", SortOrder.UNSORTED);
	        
	        squads = squadBean.getAll();
	    }

	    public Map<String, String> getColumnHeaders() {
	        return columnHeaders;
	    }
	    
	    public List<SortMeta> getSortOrders() {
	        return sortOrders;
	    }

	    public void setSortOrders(List<SortMeta> sortOrders) {
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
	        for (final SortMeta entry : sortOrders) {
	            SortOrder newOrder;

	            if (entry.getSortField().equals(sortProperty)) {
	                if (entry.getSortOrder() == SortOrder.ASCENDING) {
	                    newOrder = SortOrder.DESCENDING;
	                } else {
	                    newOrder = SortOrder.DESCENDING;
	                }
	            } else {
	                newOrder = SortOrder.UNSORTED;
	            }

	            entry.setSortOrder(newOrder);
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
