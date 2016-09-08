/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.PhaseId;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.ExtendedDataModel;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jboss.logging.Logger;
import org.joda.time.DateTime;
import org.richfaces.component.SortOrder;
import org.richfaces.component.util.Strings;
import org.richfaces.model.Arrangeable;
import org.richfaces.model.ArrangeableState;
import org.richfaces.model.FilterField;
import org.richfaces.model.SortField;

import com.google.common.collect.Lists;

import at.tfr.pfad.model.PrimaryKeyHolder;

public abstract class DataModel<T extends PrimaryKeyHolder, U extends T> extends ExtendedDataModel<U> implements Arrangeable {

    private static final int ABSOLUTE_MAX_ROWS = 200;
	private Logger log = Logger.getLogger(getClass());
    private Long rowKey;
    private ArrangeableState arrangeableState;
    private boolean isRenderResponse;
    protected Class<T> entityClass;
    protected Class<U> uiClass;

    @Inject
    protected SessionBean sessionBean;
    @Inject
    protected EntityManager entityManager;
    
    protected int rows = 20;
    protected int currentRows;
    protected Integer rowCount;
    protected List<U> uData;
    protected boolean useUniquResultTransformer = false;
    protected SequenceRange sequenceRange;
    protected String entityIdProperty = "id";
    private boolean rowsSetToUnlimited;
    protected CriteriaBuilder cb;
    protected Root<T> root;
    private CriteriaQuery<T> query;
    private CriteriaQuery<Long> countQuery;

    public DataModel() {
    }

    public DataModel(final Class<T> entityClass) {
        this.entityClass = entityClass;
        this.uiClass = (Class<U>) entityClass;
    }

    public DataModel(final Class<U> uiClass, final Class<T> entityClass) {
        this.entityClass = entityClass;
        this.uiClass = uiClass;
    }

    public List<U> convertToUiBean(final List<T> list) {
        return (List<U>) list;
    }

    @Override
    public void arrange(final FacesContext context, final ArrangeableState state) {
		if (!isRenderResponse && FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
			isRenderResponse = true;
			uData = null;
		}
        arrangeableState = state;
    }

    @Override
    public void setRowKey(final Object key) {
        rowKey = (Long) key;
    }

    @Override
    public Long getRowKey() {
        return rowKey;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(final int rows) {
        rowsSetToUnlimited = (rows == Integer.MAX_VALUE ? true : false);

        this.rows = rows;
    }

    protected CriteriaQuery<T> createCriteria(boolean addOrder) {
        cb = entityManager.getCriteriaBuilder();
		query = cb.createQuery(entityClass);
        root = query.from(entityClass);
        root.alias(entityClass.getSimpleName());
        return query;
    }

    protected CriteriaQuery<Long> createCountCriteriaQuery() {
        cb = entityManager.getCriteriaBuilder();
        countQuery = cb.createQuery(Long.class);
        root = countQuery.from(entityClass);
        root.alias(entityClass.getSimpleName());
        
        countQuery.select(cb.countDistinct(root));
        
        final List<Predicate> filterCriteria = createFilterCriteria(countQuery);
        if (filterCriteria != null) {
            countQuery.where(filterCriteria.toArray(new Predicate[]{}));
        }

        return countQuery;
    }

    protected CriteriaQuery<T> createSelectCriteriaQuery() {
        final CriteriaQuery<T> criteria = createCriteria(true);

        final List<Order> orders = createOrders();
        if (orders != null && !orders.isEmpty()) {
            criteria.orderBy(orders);
        }

        final List<Predicate> filterCriteria = createFilterCriteria(criteria);
        if (filterCriteria != null) {
            criteria.where(filterCriteria.toArray(new Predicate[]{}));
        }

        return criteria;
    }

    protected List<Order> createOrders() {
        final List<Order> orders = Lists.newArrayList();

        if (null == arrangeableState) {
            return orders;
        }

        final List<SortField> sortFields = arrangeableState.getSortFields();
        if (sortFields != null && !sortFields.isEmpty()) {

            final FacesContext facesContext = FacesContext.getCurrentInstance();

            for (final SortField sortField : sortFields) {
                final String propertyName = 
                		getEntitySortProperty((String) sortField.getSortBy().getValue(facesContext.getELContext()));

                Order order;
                final SortOrder sortOrder = sortField.getSortOrder();
                if (sortOrder == SortOrder.ascending) {
                    order = cb.asc(getPathForOrder(propertyName));
                } else if (sortOrder == SortOrder.descending) {
                    order = cb.desc(getPathForOrder(propertyName));
                } else {
                    throw new IllegalArgumentException(sortOrder.toString());
                }

                orders.add(order);
            }
        }

        return orders;
    }

	protected Path<Object> getPathForOrder(final String propertyName) {
		return root.get(propertyName);
	}
    
    protected String getEntitySortProperty(String sortField) {
    	return sortField;
    }

    protected ArrangeableState getArrangeableState() {
        return arrangeableState;
    }

    protected Class<U> getEntityClass() {
        return uiClass;
    }

    protected Predicate createFilterCriteriaForField(final String propertyName, final Object filterValue, CriteriaQuery<?> q) {
        if (filterValue == null || (filterValue instanceof String && Strings.isEmpty((String) filterValue))) {
            return null;
        }

        try {
            final Method method = entityClass.getMethod("get" + StringUtils.capitalize(propertyName), new Class[] {});
            if (method != null) {
                final Class<?> returnType = method.getReturnType();
                if (Date.class.isAssignableFrom(returnType)) {
                    final String value = filterValue.toString().trim();
                    final DateTime date = new DateTime(DateUtils.parseDate(value, "dd", "dd.MM", "dd.MM.yyyy", "dd HH", "dd.MM HH", "dd.MM.YYYY HH"));

                    if (value.contains(" ")) { // the hour was specified
                        return cb.between(root.get(propertyName), date.toDate(), date.plusHours(1).toDate());
                    }
                    return cb.between(root.get(propertyName), date.toDate(), date.plusDays(1).toDate());

                } else if (Integer.class.isAssignableFrom(method.getReturnType())) {
                    return cb.equal(getPathForOrder(propertyName), Integer.parseInt(filterValue.toString()));

                } else if (Long.class.isAssignableFrom(method.getReturnType())) {
                    return cb.equal(getPathForOrder(propertyName), Long.parseLong(filterValue.toString()));

                } else if (Float.class.isAssignableFrom(method.getReturnType())) {
                    return cb.equal(getPathForOrder(propertyName), Float.parseFloat(filterValue.toString()));

                } else if (Double.class.isAssignableFrom(method.getReturnType())) {
                    return cb.equal(getPathForOrder(propertyName), Double.parseDouble(filterValue.toString()));

                } else if (Boolean.class.isAssignableFrom(method.getReturnType())) {
                    return cb.equal(getPathForOrder(propertyName), Boolean.parseBoolean(filterValue.toString()));

                } else if (filterValue instanceof String) {
                    String stringValue = (String) filterValue;
                    stringValue = stringValue.toLowerCase(arrangeableState.getLocale());
                    return cb.like(cb.lower(root.get(propertyName)), "%"+stringValue+"%");
                }
            }
        } catch (final Exception e) {
            log.debug("cannot handle value=" + filterValue + " for property=" + propertyName, e);
        }

        return cb.like(root.get(propertyName), filterValue.toString());
    }
    
    private List<Predicate> createFilterCriteria(CriteriaQuery<?> criteriaQuery) {
        List<Predicate> filterCriteria = new ArrayList<>();

        if (sessionBean.isLeiter()) {
//            filterCriteria = Restrictions.conjunction();
//            filterCriteria.add(Restrictions.eq(getAccountProperty(), getAccountValue(smsUsers)));
        }

        if (null == arrangeableState) {
            return filterCriteria;
        }

        final List<FilterField> filterFields = arrangeableState.getFilterFields();
        if (filterFields != null && !filterFields.isEmpty()) {
            final FacesContext facesContext = FacesContext.getCurrentInstance();

            for (final FilterField filterField : filterFields) {
                final String propertyName = (String) filterField.getFilterExpression().getValue(facesContext.getELContext());
                final Object filterValue = filterField.getFilterValue();

                final Predicate crit = createFilterCriteriaForField(propertyName, filterValue, criteriaQuery);

                if (crit == null) {
                    continue;
                }

                filterCriteria.add(crit);
            }
        }
        return filterCriteria;
    }

    @Override
    public void walk(final FacesContext context, final DataVisitor visitor, final Range range, final Object argument) {
        final List<U> uData = getRowData(range);

        for (final U t : uData) {
            visitor.process(context, t.getId(), argument);
        }
    }

    protected List<U> getRowData(final Range range) {
    	if (uData == null) {
    		uData = getRowDataInternal(range);
    	}
    	return uData;
    }

	private List<U> getRowDataInternal(final Range range) {
		final CriteriaQuery<T> criteria = createSelectCriteriaQuery();
		criteria.distinct(true);

        TypedQuery<T> query = entityManager.createQuery(criteria);
        sequenceRange = (SequenceRange) range;
        if (sequenceRange.getFirstRow() >= 0) {
            query.setFirstResult(sequenceRange.getFirstRow());
        }
        query.setMaxResults(sequenceRange.getRows() > 0 ? sequenceRange.getRows() : ABSOLUTE_MAX_ROWS);

        final List<T> data = query.getResultList();
        currentRows = data.size();
        uData = convertToUiBean(data);
        return uData;
	}

    @Override
    public boolean isRowAvailable() {
        return rowKey != null;
    }

    @Override
    public int getRowCount() {
    	if (rowCount == null) {	
    		rowCount = getRowCountInternal();
    	}
    	return rowCount;
    }

	private int getRowCountInternal() {
		final CriteriaQuery<Long> criteria = createCountCriteriaQuery();
        final Long count = entityManager.createQuery(criteria).getSingleResult();
        return count.intValue();
	}

    @Override
    public U getRowData() {
        try {
            U ud = (U)uData.stream().filter(u->u.getId().equals(rowKey)).findAny().orElse(null);
			return ud;
        } catch (Exception e) {
            log.info("error loading key: " + rowKey, e);
            throw e;
        }
    }

    @Override
    public int getRowIndex() {
        return -1;
    }

    @Override
    public void setRowIndex(final int rowIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getWrappedData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWrappedData(final Object data) {
        throw new UnsupportedOperationException();
    }

    public int getCurrentRows() {
        return currentRows;
    }

    public void setCurrentRows(final int currentRows) {
        this.currentRows = currentRows;
    }

    public boolean isRenderScroller() {
        return currentRows == rows || sequenceRange == null || sequenceRange.getFirstRow() > 0;
    }

    public void setRowsUnlimited() {
        setRows(Integer.MAX_VALUE);
    }

    public boolean isRowsSetToUnlimited() {
        return rowsSetToUnlimited;
    }

    public boolean isUseUniquResultTransformer() {
        return useUniquResultTransformer;
    }

    public void setUseUniquResultTransformer(final boolean useUniquResultTransformer) {
        this.useUniquResultTransformer = useUniquResultTransformer;
    }

    public Class<U> getUiClass() {
        return uiClass;
    }

    public void clear() {
        currentRows = 0;
        uData = null;
    }

    public void clearSelection(AjaxBehaviorEvent event) {
    }
    
}
