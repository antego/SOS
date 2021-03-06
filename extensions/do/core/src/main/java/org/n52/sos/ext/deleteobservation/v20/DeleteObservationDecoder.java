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
package org.n52.sos.ext.deleteobservation.v20;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.n52.sos.ext.deleteobservation.DeleteObservationConstants.CONFORMANCE_CLASSES;
import static org.n52.sos.ext.deleteobservation.DeleteObservationConstants.NS_SOSDO_2_0;
import static org.n52.sos.ogc.sos.SosConstants.SOS;
import static org.n52.sos.util.CodingHelper.decoderKeysForElements;
import static org.n52.sos.util.CodingHelper.xmlDecoderKeysForOperation;
import static org.n52.sos.util.CollectionHelper.union;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.decode.Decoder;
import org.n52.sos.decode.DecoderKey;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.UnsupportedDecoderInputException;
import org.n52.sos.ext.deleteobservation.DeleteObservationConstants;
import org.n52.sos.ext.deleteobservation.DeleteObservationRequest;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

import net.opengis.sosdo.x20.DeleteObservationDocument;
import net.opengis.sosdo.x20.DeleteObservationType;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk
 *         J&uuml;rrens</a>
 * 
 * @since 1.0.0
 */
public class DeleteObservationDecoder implements Decoder<DeleteObservationRequest, XmlObject> {

    @SuppressWarnings("unchecked")
    private static final Set<DecoderKey> DECODER_KEYS =
            union(decoderKeysForElements(NS_SOSDO_2_0, DeleteObservationDocument.class), xmlDecoderKeysForOperation(
                    SOS, Sos2Constants.SERVICEVERSION, DeleteObservationConstants.Operations.DeleteObservation));

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteObservationDecoder.class);

    public DeleteObservationDecoder() {
        LOGGER.info("Decoder for the following keys initialized successfully: {}!",
                Joiner.on(", ").join(DECODER_KEYS));
    }

    public Set<DecoderKey> getDecoderKeyTypes() {
        return Collections.unmodifiableSet(DECODER_KEYS);
    }

    public DeleteObservationRequest decode(XmlObject xmlObject) throws OwsExceptionReport {
        LOGGER.debug(format("REQUESTTYPE: %s", xmlObject != null ? xmlObject.getClass() : "null recevied"));
        // XmlHelper.validateDocument(xmlObject);
        if (xmlObject instanceof DeleteObservationDocument) {
            DeleteObservationDocument delObsDoc = (DeleteObservationDocument) xmlObject;
            DeleteObservationRequest decodedRequest = parseDeleteObservation(delObsDoc);
            LOGGER.debug(String.format("Decoded request: %s", decodedRequest));
            return decodedRequest;
        } else {
            throw new UnsupportedDecoderInputException(this, xmlObject);
        }
    }

    private DeleteObservationRequest parseDeleteObservation(DeleteObservationDocument xbDelObsDoc)
            throws OwsExceptionReport {
        DeleteObservationRequest delObsRequest = null;

        DeleteObservationType xbDelObsType = xbDelObsDoc.getDeleteObservation();

        if (xbDelObsType != null) {
            delObsRequest = new DeleteObservationRequest(NS_SOSDO_2_0);
            delObsRequest.setVersion(xbDelObsType.getVersion());
            delObsRequest.setService(xbDelObsType.getService());
            if (CollectionHelper.isNotNullOrEmpty(xbDelObsType.getObservationArray())) {
                delObsRequest.setObservationIdentifiers(Sets.newHashSet(xbDelObsType.getObservationArray()));
            } else {
                parseProcedure(xbDelObsType, delObsRequest);
                parseObservedProperty(xbDelObsType, delObsRequest);
                parseFeatureOfInterest(xbDelObsType, delObsRequest);
                parseOffering(xbDelObsType, delObsRequest);
                parseTemporalFilter(xbDelObsType, delObsRequest);
            }
        } else {
            throw new NoApplicableCodeException()
                    .withMessage("Received XML document is not valid. Set log level to debug to get more details");
        }

        return delObsRequest;
    }

    private void parseProcedure(DeleteObservationType dot, DeleteObservationRequest request) {
        if (CollectionHelper.isNotNullOrEmpty(dot.getProcedureArray())) {
            request.setProcedures(Sets.newHashSet(dot.getProcedureArray()));
        }
    }

    private void parseObservedProperty(DeleteObservationType dot, DeleteObservationRequest request) {
        if (CollectionHelper.isNotNullOrEmpty(dot.getObservedPropertyArray())) {
            request.setObservedProperties(Sets.newHashSet(dot.getObservedPropertyArray()));
        }
    }

    private void parseFeatureOfInterest(DeleteObservationType dot, DeleteObservationRequest request) {
        if (CollectionHelper.isNotNullOrEmpty(dot.getFeatureOfInterestArray())) {
            request.setFeatureIdentifiers(Sets.newHashSet(dot.getFeatureOfInterestArray()));
        }
    }

    private void parseOffering(DeleteObservationType dot, DeleteObservationRequest request) {
        if (CollectionHelper.isNotNullOrEmpty(dot.getOfferingArray())) {
            request.setOfferings(Sets.newHashSet(dot.getOfferingArray()));
        }
    }

    private void parseTemporalFilter(DeleteObservationType dot, DeleteObservationRequest request) throws OwsExceptionReport {
        if (CollectionHelper.isNotNullOrEmpty(dot.getTemporalFilterArray())) {
            request.setTemporalFilters(parseTemporalFilters(dot.getTemporalFilterArray()));
        }
    }
    
    private Set<TemporalFilter> parseTemporalFilters(
            final net.opengis.sosdo.x20.DeleteObservationType.TemporalFilter[] temporalFilters) throws OwsExceptionReport {
        final Set<TemporalFilter> sosTemporalFilters = Sets.newHashSetWithExpectedSize(temporalFilters.length);
        for (final net.opengis.sosdo.x20.DeleteObservationType.TemporalFilter temporalFilter : temporalFilters) {
            final Object filter = CodingHelper.decodeXmlElement(temporalFilter.getTemporalOps());
            if (filter instanceof TemporalFilter) {
                sosTemporalFilters.add((TemporalFilter) filter);
            }
        }
        return sosTemporalFilters;
    }

    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return emptyMap();
    }

    public Set<String> getConformanceClasses() {
        return CONFORMANCE_CLASSES;
    }

}
