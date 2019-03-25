/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.rest.internal;

import com.gene42.commons.utils.exceptions.ServiceException;

import org.phenotips.data.api.EntitySearch;
import org.phenotips.data.api.EntitySearchResult;
import org.phenotips.data.rest.LiveTableGenerator;
import org.phenotips.data.rest.LiveTableInputAdapter;
import org.phenotips.data.rest.LiveTableSearch;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.model.reference.DocumentReference;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 * This is the default implementation of the LiveTableSearch REST API.
 *
 * @version $Id$
 */
@SuppressWarnings({ "checkstyle:classfanoutcomplexity", "checkstyle:classdataabstractioncoupling" })
@Component
@Named(DefaultLiveTableSearchImpl.NAME)
@InstantiationStrategy(ComponentInstantiationStrategy.SINGLETON)
@Singleton
public class DefaultLiveTableSearchImpl implements LiveTableSearch
{
    /** Name of this Component. */
    public static final String NAME = "org.phenotips.data.rest.internal.DefaultLiveTableSearchImpl";

    @Inject
    private LiveTableGenerator<DocumentReference> liveTableGenerator;

    @Inject
    private LiveTableFacade liveTableFacade;

    @Inject
    private Logger logger;

    @Inject
    private EntitySearch<DocumentReference> documentSearch;

    @Inject
    @Named("url")
    private LiveTableInputAdapter inputAdapter;

    @Override
    public Response search()
    {
        try {
            LiveTableStopWatches stopWatches = new LiveTableStopWatches();

            Map<String, List<String>> queryParameters = this.liveTableFacade.getQueryParameters();

            stopWatches.getAdapterStopWatch().start();
            JSONObject inputObject = this.inputAdapter.convert(queryParameters);
            stopWatches.getAdapterStopWatch().stop();

            return this.search(inputObject, queryParameters, stopWatches);
        } catch (SecurityException e) {
            this.handleError(e, Status.UNAUTHORIZED);
        } catch (ServiceException | IllegalArgumentException e) {
            this.handleError(e, Status.BAD_REQUEST);
        }

        return Response.serverError().build();
    }

    @Override
    public Response search(String jsonContent) {
        try {
            return this.search(new JSONObject(jsonContent), Collections.emptyMap(), new LiveTableStopWatches());
        } catch (SecurityException e) {
            this.handleError(e, Status.UNAUTHORIZED);
        } catch (ServiceException | JSONException | IllegalArgumentException e) {
            this.handleError(e, Status.BAD_REQUEST);
        }

        return Response.serverError().build();
    }

    private Response search(JSONObject inputObject, Map<String, List<String>> queryParameters,
        LiveTableStopWatches stopWatches) throws ServiceException {

        this.liveTableFacade.authorizeEntitySearchInput(inputObject);

        stopWatches.getSearchStopWatch().start();
        EntitySearchResult<DocumentReference> documentSearchResult = this.documentSearch.search(inputObject);
        stopWatches.getSearchStopWatch().stop();

        stopWatches.getTableStopWatch().start();
        JSONObject responseObject =
            this.liveTableGenerator.generateTable(documentSearchResult, inputObject, queryParameters);
        stopWatches.getTableStopWatch().stop();

        JSONObject timingsJSON = stopWatches.toJSONObject();
        responseObject.put(LiveTableSearch.Keys.TIMINGS, timingsJSON);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug(timingsJSON.toString(4));
        }

        Response.ResponseBuilder response = Response.ok(responseObject, MediaType.APPLICATION_JSON_TYPE);

        return response.build();
    }

    private void handleError(Exception e, Status status)
    {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Error encountered", e);
        }
        throw new WebApplicationException(e, status);
    }
}
