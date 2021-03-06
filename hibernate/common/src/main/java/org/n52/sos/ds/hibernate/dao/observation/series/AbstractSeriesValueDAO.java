/**
 * Copyright (C) 2012-2017 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sos.ds.hibernate.dao.observation.series;

import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.hibernate.dao.observation.AbstractValueDAO;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.Unit;
import org.n52.sos.ds.hibernate.entities.observation.legacy.AbstractValuedLegacyObservation;
import org.n52.sos.ds.hibernate.entities.observation.series.AbstractValuedSeriesObservation;
import org.n52.sos.ds.hibernate.entities.observation.series.Series;
import org.n52.sos.ds.hibernate.util.HibernateHelper;
import org.n52.sos.ds.hibernate.util.observation.ExtensionFesFilterCriteriaAdder;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.request.AbstractObservationRequest;
import org.n52.sos.request.GetObservationRequest;
import org.n52.sos.util.CollectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract value data access object class for {@link SeriesValue}
 * 
 * @author Carsten Hollmann <c.hollmann@52north.org>
 * @since 4.3.0
 *
 */
public abstract class AbstractSeriesValueDAO extends AbstractValueDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSeriesValueDAO.class);

    protected abstract Class<?> getSeriesValueClass();

    /**
     * Query streaming value for parameter as {@link ScrollableResults}
     * 
     * @param request
     *            {@link AbstractObservationRequest}
     * @param series
     *            Datasource series id
     * @param temporalFilterCriterion
     *            Temporal filter {@link Criterion}
     * @param session
     *            Hibernate Session
     * @return Resulting {@link ScrollableResults}
     * @throws HibernateException
     *             If an error occurs when querying the {@link AbstractValue}s
     * @throws OwsExceptionReport
     *             If an error occurs when querying the {@link AbstractValue}s
     */
    public ScrollableResults getStreamingSeriesValuesFor(AbstractObservationRequest request, long series,
            Criterion temporalFilterCriterion, Session session) throws OwsExceptionReport {
        return getSeriesValueCriteriaFor(request, series, temporalFilterCriterion, session).scroll(
                ScrollMode.FORWARD_ONLY);
    }

    public ScrollableResults getStreamingSeriesValuesFor(AbstractObservationRequest request, Set<Long> series,
            Criterion temporalFilterCriterion, Session session) throws HibernateException, OwsExceptionReport {
        return getSeriesValueCriteriaFor(request, series, temporalFilterCriterion, session).scroll(
                ScrollMode.FORWARD_ONLY);
    }

    /**
     * Query streaming value for parameter as {@link ScrollableResults}
     * 
     * @param request
     *            {@link AbstractObservationRequest}
     * @param series
     *            Datasource series id
     * @param session
     *            Hibernate Session
     * @return Resulting {@link ScrollableResults}
     * @throws OwsExceptionReport
     *             If an error occurs when querying the {@link AbstractValue}s
     */
    public ScrollableResults getStreamingSeriesValuesFor(AbstractObservationRequest request, long series, Session session)
            throws OwsExceptionReport {
        return getSeriesValueCriteriaFor(request, series, null, session).scroll(ScrollMode.FORWARD_ONLY);
    }

    public ScrollableResults getStreamingSeriesValuesFor(AbstractObservationRequest request, Set<Long> series,
            Session session) throws HibernateException, OwsExceptionReport {
        return getSeriesValueCriteriaFor(request, series, null, session).scroll(ScrollMode.FORWARD_ONLY);
    }

    /**
     * Query streaming value for parameter as chunk {@link List}
     * 
     * @param request
     *            {@link AbstractObservationRequest}
     * @param series
     *            Datasource series id
     * @param temporalFilterCriterion
     *            Temporal filter {@link Criterion}
     * @param chunkSize
     *            chunk size
     * @param currentRow
     *            Start row
     * @param session
     *            Hibernate Session
     * @return Resulting chunk {@link List}
     * @throws OwsExceptionReport
     *             If an error occurs when querying the {@link AbstractValue}s
     */
    @SuppressWarnings("unchecked")
    public List<AbstractValuedLegacyObservation<?>> getStreamingSeriesValuesFor(AbstractObservationRequest request, long series,
            Criterion temporalFilterCriterion, int chunkSize, int currentRow, Session session)
            throws OwsExceptionReport {
        Criteria c = getSeriesValueCriteriaFor(request, series, temporalFilterCriterion, session);
        addChunkValuesToCriteria(c, chunkSize, currentRow, request);
        return (List<AbstractValuedLegacyObservation<?>>) c.list();
    }
    
    /**
     * Query streaming value for parameter as chunk {@link List}
     * 
     * @param request
     *            {@link AbstractObservationRequest}
     * @param series
     *            Datasource series id
     * @param temporalFilterCriterion
     *            Temporal filter {@link Criterion}
     * @param chunkSize
     *            chunk size
     * @param currentRow
     *            Start row
     * @param session
     *            Hibernate Session
     * @return Resulting chunk {@link List}
     * @throws OwsExceptionReport
     *             If an error occurs when querying the {@link AbstractValue}s
     */
    @SuppressWarnings("unchecked")
    public List<AbstractValuedLegacyObservation<?>> getStreamingSeriesValuesFor(AbstractObservationRequest request, Set<Long> series,
            Criterion temporalFilterCriterion, int chunkSize, int currentRow, Session session)
            throws OwsExceptionReport {
        Criteria c = getSeriesValueCriteriaFor(request, series, temporalFilterCriterion, session);
        addChunkValuesToCriteria(c, chunkSize, currentRow, request);
        return (List<AbstractValuedLegacyObservation<?>>) c.list();
    }

    /**
     * Query streaming value for parameter as chunk {@link List}
     * 
     * @param request
     *            {@link AbstractObservationRequest}
     * @param series
     *            Datasource series ids
     * @param chunkSize
     *            Chunk size
     * @param currentRow
     *            Start row
     * @param session
     *            Hibernate Session
     * @return Resulting chunk {@link List}
     * @throws OwsExceptionReport
     *             If an error occurs when querying the {@link AbstractValue}s
     */
    @SuppressWarnings("unchecked")
    public List<AbstractValuedLegacyObservation<?>> getStreamingSeriesValuesFor(AbstractObservationRequest request, Set<Long> series, int chunkSize,
            int currentRow, Session session) throws OwsExceptionReport {
        Criteria c = getSeriesValueCriteriaFor(request, series, null, session);
        addChunkValuesToCriteria(c, chunkSize, currentRow, request);
        return (List<AbstractValuedLegacyObservation<?>>) c.list();
    }
    
    /**
     * Query streaming value for parameter as chunk {@link List}
     * 
     * @param request
     *            {@link AbstractObservationRequest}
     * @param series
     *            Datasource series id
     * @param chunkSize
     *            Chunk size
     * @param currentRow
     *            Start row
     * @param session
     *            Hibernate Session
     * @return Resulting chunk {@link List}
     * @throws OwsExceptionReport
     *             If an error occurs when querying the {@link AbstractValue}s
     */
    @SuppressWarnings("unchecked")
    public List<AbstractValuedLegacyObservation<?>> getStreamingSeriesValuesFor(AbstractObservationRequest request, long series, int chunkSize,
            int currentRow, Session session) throws OwsExceptionReport {
        Criteria c = getSeriesValueCriteriaFor(request, series, null, session);
        addChunkValuesToCriteria(c, chunkSize, currentRow, request);
        return (List<AbstractValuedLegacyObservation<?>>) c.list();
    }

    /**
     * Get {@link Criteria} for parameter
     * 
     * @param request
     *            {@link AbstractObservationRequest}
     * @param series
     *            Datasource series id
     * @param temporalFilterCriterion
     *            Temporal filter {@link Criterion}
     * @param session
     *            Hibernate Session
     * @return Resulting {@link Criteria}
     * @throws OwsExceptionReport
     *             If an error occurs when adding Spatial Filtering Profile
     *             restrictions
     */
    private Criteria getSeriesValueCriteriaFor(AbstractObservationRequest request, long series,
            Criterion temporalFilterCriterion, Session session) throws OwsExceptionReport {
        final Criteria c = getDefaultObservationCriteria(session).createAlias(AbstractValuedSeriesObservation.SERIES, "s");
        c.addOrder(Order.asc(getOrderColumn(request)));
        c.add(Restrictions.eq("s." + Series.ID, series));
        String logArgs = "request, series";
        if (request instanceof GetObservationRequest) {
            GetObservationRequest getObsReq = (GetObservationRequest)request;
            checkAndAddSpatialFilteringProfileCriterion(c, getObsReq, session);
            if (CollectionHelper.isNotEmpty(getObsReq.getOfferings())) {
                c.createCriteria(AbstractValuedSeriesObservation.OFFERINGS).add(Restrictions.in(Offering.IDENTIFIER, getObsReq.getOfferings()));
            }
    
            logArgs += ", offerings";
            if (temporalFilterCriterion != null) {
                logArgs += ", filterCriterion";
                c.add(temporalFilterCriterion);
            }
            addSpecificRestrictions(c, getObsReq);
            if (getObsReq.isSetFesFilterExtension()) {
                new ExtensionFesFilterCriteriaAdder(c, getObsReq.getFesFilterExtensions()).add();
            }
        }
        LOGGER.debug("QUERY getStreamingSeriesValuesFor({}): {}", logArgs, HibernateHelper.getSqlString(c));
        return c.setReadOnly(true);
    }
    
    /**
     * Get {@link Criteria} for parameter
     * 
     * @param request
     *            {@link AbstractObservationRequest}
     * @param series
     *            Datasource series ids
     * @param temporalFilterCriterion
     *            Temporal filter {@link Criterion}
     * @param session
     *            Hibernate Session
     * @return Resulting {@link Criteria}
     * @throws OwsExceptionReport
     *             If an error occurs when adding Spatial Filtering Profile
     *             restrictions
     */
    private Criteria getSeriesValueCriteriaFor(AbstractObservationRequest request, Set<Long> series,
            Criterion temporalFilterCriterion, Session session) throws OwsExceptionReport {
        final Criteria c = getDefaultObservationCriteria(session).createAlias(AbstractValuedSeriesObservation.SERIES, "s");
        c.addOrder(Order.asc(getOrderColumn(request)));
        c.add(Restrictions.in("s." + Series.ID, series));
        String logArgs = "request, series";
        if (request instanceof GetObservationRequest) {
            GetObservationRequest getObsReq = (GetObservationRequest)request;
            checkAndAddSpatialFilteringProfileCriterion(c, getObsReq, session);
            if (CollectionHelper.isNotEmpty(getObsReq.getOfferings())) {
                c.createCriteria(AbstractValuedSeriesObservation.OFFERINGS).add(Restrictions.in(Offering.IDENTIFIER, getObsReq.getOfferings()));
            }
    
            logArgs += ", offerings";
            if (temporalFilterCriterion != null) {
                logArgs += ", filterCriterion";
                c.add(temporalFilterCriterion);
            }
            addSpecificRestrictions(c, getObsReq);
        }
        LOGGER.debug("QUERY getStreamingSeriesValuesFor({}): {}", logArgs, HibernateHelper.getSqlString(c));
        return c.setReadOnly(true);
    }

    /**
     * Get default {@link Criteria} for {@link Class}
     * 
     * @param clazz
     *            {@link Class} to get default {@link Criteria} for
     * @param session
     *            Hibernate Session
     * @return Default {@link Criteria}
     */
    protected Criteria getDefaultObservationCriteria(Session session) {
        return getDefaultCriteria(getSeriesValueClass(), session);
//        return session.createCriteria(getSeriesValueClass()).add(Restrictions.eq(AbstractValuedSeriesObservation.DELETED, false))
//                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    }

    /**
     * Query unit for parameter
     * 
     * @param request
     *            {@link AbstractObservationRequest}
     * @param series
     *            Datasource series id
     * @param session
     *            Hibernate Session
     * @return Unit or null if no unit is set
     * @throws OwsExceptionReport
     *             If an error occurs when querying the unit
     */
    public String getUnit(AbstractObservationRequest request, long series, Session session) throws OwsExceptionReport {
        Criteria c = getSeriesValueCriteriaFor(request, series, null, session);
        Unit unit = (Unit) c.setMaxResults(1).setProjection(Projections.property(AbstractValuedSeriesObservation.UNIT)).uniqueResult();
        if (unit != null && unit.isSetUnit()) {
            return unit.getUnit();
        }
        return null;
    }
    
    /**
     * Query unit for parameter
     * 
     * @param request
     *            {@link AbstractObservationRequest}
     * @param series
     *            Datasource series id
     * @param session
     *            Hibernate Session
     * @return Unit or null if no unit is set
     * @throws OwsExceptionReport
     *             If an error occurs when querying the unit
     */
    public String getUnit(AbstractObservationRequest request, Set<Long> series, Session session) throws OwsExceptionReport {
        Criteria c = getSeriesValueCriteriaFor(request, series, null, session);
        Unit unit = (Unit) c.setMaxResults(1).setProjection(Projections.property(AbstractValuedSeriesObservation.UNIT)).uniqueResult();
        if (unit != null && unit.isSetUnit()) {
            return unit.getUnit();
        }
        return null;
    }

}
