/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.rest;

import org.phenotips.rest.PATCH;

import org.xwiki.rest.XWikiRestComponent;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Interface for Cohorts.
 *
 * @version $Id$
 */
public interface ResourceRestEndpoint extends XWikiRestComponent
{
    /**
     * Creates a new cohort and populates it with the contents of the given JSON.
     * @return a Response object containing the cohort with its generated id
     */
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    Response create(String jsonString);

    /**
     * Returns a cohort as JSON.
     * @param cohortId the id of the cohort
     * @return a Response object containing the cohort
     */

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    Response get(@PathParam(ID) String cohortId);

    /**
     * Creates a new cohort and populates it with the contents of the given JSON.
     * @return a Response object containing the cohort with its generated id
     */
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PATCH
    Response update(@PathParam(ID) String cohortId, String jsonString);

    /**
     * Deletes the cohort document from the system. It also triggers a job to remove all variants
     * affected by this cohort.
     * @param cohortId the id of the cohort*
     * @return empty body
     */
    @Produces(MediaType.APPLICATION_JSON)
    @DELETE
    Response delete(
        @PathParam(ID) String cohortId);

    /**
     * Returns a list of cohorts.
     * @param offset the offset of the query
     * @param limit the size limit of the result set
     * @param filter the filter parameters
     * @param sort the sort properties and orders
     * @return a Response object containing the JSON payload
     */
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @GET
    Response search(
        @DefaultValue("0") @QueryParam(OFFSET) int offset,
        @DefaultValue("25") @QueryParam(LIMIT) int limit,
        @QueryParam(FILTER) List<String> filter,
        @DefaultValue("doc.name") @QueryParam(SORT) List<String> sort
    );

    String OFFSET = "offset";
    String LIMIT = "limit";
    String FILTER = "filter";
    String SORT = "sort";

    String ID = "{id}";
}
