package at.tfr.pfad.view;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.richfaces.component.SortOrder;

@Named
@ViewScoped
public class BookingTableBean implements Serializable {

	    private static int cnt = 0;
	    private String selectionMode = "multiple";
	    private Map<String, SortOrder> sortOrders = new HashMap<String, SortOrder>();
	    private Map<String, String> columnHeaders = new HashMap<String, String>();
	    private Map<String, String> filterValues = new HashMap<String, String>();
	    private String sortProperty;

	    @Inject
	    private transient BookingDataModel bookingDataModel;

	    public BookingTableBean() {
	        sortOrders.put("id", SortOrder.unsorted);
	        sortOrders.put("member", SortOrder.unsorted);
	        sortOrders.put("activity", SortOrder.unsorted);
	        sortOrders.put("squad", SortOrder.unsorted);
	        sortOrders.put("status", SortOrder.unsorted);
	        sortOrders.put("comment", SortOrder.unsorted);
	    }

	    @PostConstruct
	    public void postConstruct() {
	        columnHeaders.put("id", "ID");
	        columnHeaders.put("member", "Mitglied");
	        columnHeaders.put("activity", "Aktivität");
	        columnHeaders.put("squad", "Trupp");
	        columnHeaders.put("status", "Status");
	        columnHeaders.put("payed", "Bezahlt");
	        columnHeaders.put("comment", "Bemerkung");
	    }

	    public Map<String, String> getColumnHeaders() {
	        return columnHeaders;
	    }

	    public Map<String, SortOrder> getSortOrders() {
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
