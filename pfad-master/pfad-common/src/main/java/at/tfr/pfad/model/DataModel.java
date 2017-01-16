/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.jpa.QueryHints;
import org.jboss.logging.Logger;
import org.joda.time.DateTime;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import at.tfr.pfad.dao.BookingRepository;
import at.tfr.pfad.dao.PaymentRepository;

@SuppressWarnings("serial")
public abstract class DataModel<T extends PrimaryKeyHolder, U extends T> extends LazyDataModel<U> {

    private static final int ABSOLUTE_MAX_ROWS = 200;
	private Logger log = Logger.getLogger(getClass());
    protected Class<T> entityClass;
    protected Class<U> uiClass;

    @Inject
    protected EntityManager entityManager;
    @Inject
    protected BookingRepository bookingRepo;
    @Inject
    protected PaymentRepository paymentRepo;
    @Inject
    protected Locale locale;
    
    protected boolean useUniquResultTransformer = false;
    protected String entityIdProperty = "id";
    protected CriteriaBuilder cb;
    protected Root<T> root;
    private CriteriaQuery<T> query;
    private CriteriaQuery<Long> countQuery;

    public DataModel() {
    }

    @SuppressWarnings("unchecked")
	public DataModel(final Class<T> entityClass) {
        this.entityClass = entityClass;
        this.uiClass = (Class<U>) entityClass;
    }

    public DataModel(final Class<U> uiClass, final Class<T> entityClass) {
        this.entityClass = entityClass;
        this.uiClass = uiClass;
    }

    @Override
    public Long getRowKey(U entry) {
        return entry.getId();
    }

    protected CriteriaQuery<T> createCriteria(boolean addOrder) {
        cb = entityManager.getCriteriaBuilder();
		query = cb.createQuery(entityClass);
        root = query.from(entityClass);
        root.alias(entityClass.getSimpleName());
        query.select(root);
        return query;
    }

    protected CriteriaQuery<Long> createCountCriteriaQuery(Map<String, Object> filters) {
        cb = entityManager.getCriteriaBuilder();
        countQuery = cb.createQuery(Long.class);
        root = countQuery.from(entityClass);
        
        countQuery.select(cb.countDistinct(root));
        
        final List<Predicate> filterCriteria = createFilterCriteria(countQuery, filters);
        if (filterCriteria != null) {
            countQuery.where(filterCriteria.toArray(new Predicate[]{}));
        }

        return countQuery;
    }

    protected CriteriaQuery<T> createSelectCriteriaQuery(List<SortMeta> sortMetas, Map<String,Object> filters) {
        final CriteriaQuery<T> criteria = createCriteria(true);

        final List<Order> orders = createOrders(sortMetas);
        if (orders != null && !orders.isEmpty()) {
            criteria.orderBy(orders);
        }

        final List<Predicate> filterCriteria = createFilterCriteria(criteria, filters);
        if (filterCriteria != null) {
            criteria.where(filterCriteria.toArray(new Predicate[]{}));
        }

        return criteria;
    }

    protected List<Order> createOrders(List<SortMeta> sortMetas) {
        final List<Order> orders = Lists.newArrayList();

        if (sortMetas != null && !sortMetas.isEmpty()) {

            for (final SortMeta sortMeta : sortMetas) {
                final String propertyName = 
                		getEntitySortProperty(sortMeta.getSortField());

                Order order;
                final SortOrder sortOrder = sortMeta.getSortOrder();
                if (sortOrder == SortOrder.ASCENDING) {
                    order = cb.asc(getPathForOrder(propertyName));
                } else if (sortOrder == SortOrder.DESCENDING) {
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
    
    protected String getEntitySortProperty(String SortMeta) {
    	return SortMeta;
    }

    protected Class<T> getEntityClass() {
        return entityClass;
    }

    protected Predicate createFilterCriteriaForField(final String propertyName, final Object filterValue, CriteriaQuery<?> q) {
        if (filterValue == null || (filterValue instanceof String && Strings.isNullOrEmpty((String) filterValue))) {
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
                    stringValue = stringValue.toLowerCase();
                    return cb.like(cb.lower(root.get(propertyName)), "%"+stringValue+"%");
                }
            }
        } catch (final Exception e) {
            log.debug("cannot handle value=" + filterValue + " for property=" + propertyName, e);
        }

        return cb.like(root.get(propertyName), filterValue.toString());
    }
    
    private List<Predicate> createFilterCriteria(CriteriaQuery<?> criteriaQuery, Map<String,Object> filters) {
        List<Predicate> filterCriteria = new ArrayList<>();

        if (null == filters || filters.isEmpty()) {
            return filterCriteria;
        }

        for (final Entry<String,Object> filter : filters.entrySet()) {
            final String propertyName = filter.getKey();
            final Object filterValue = filter.getValue();

            final Predicate crit = createFilterCriteriaForField(propertyName, filterValue, criteriaQuery);

            if (crit == null) {
                continue;
            }

            filterCriteria.add(crit);
        }
        return filterCriteria;
    }

    @Override
    public List<U> load(int first, int pageSize, List<SortMeta> sortMetas, Map<String, Object> filters) {
        final List<U> uData = getRowDataInternal(first, pageSize, sortMetas, filters);
        setRowCount(getRowCountInternal(filters));
        return uData;
    }

	protected List<U> getRowDataInternal(final int first, final int pageSize, List<SortMeta> sortMetas, Map<String, Object> filters) {
		final CriteriaQuery<T> criteria = createSelectCriteriaQuery(sortMetas, filters);
		groupBy(criteria);

        TypedQuery<T> query = entityManager.createQuery(criteria);
        if (first >= 0) {
            query.setFirstResult(first);
        }
        query.setMaxResults(pageSize > 0 ? pageSize : ABSOLUTE_MAX_ROWS);

        return query.getResultList().stream().map(t->convert(t)).collect(Collectors.toList());
	}
	
	public abstract U convert(T entity);

	protected CriteriaQuery<T> groupBy(CriteriaQuery<T> crit) {
		//crit.groupBy(root.get("id"));
		return crit;
	}

	public int getRowCountInternal(Map<String, Object> filters) {
		final CriteriaQuery<Long> criteria = createCountCriteriaQuery(filters);
        final Long count = entityManager.createQuery(criteria).getSingleResult();
        return count.intValue();
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

    public void clearSelection(AjaxBehaviorEvent event) {
    }
    
}
