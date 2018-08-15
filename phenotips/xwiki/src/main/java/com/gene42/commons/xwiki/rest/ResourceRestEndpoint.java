/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.xwiki.rest;

import org.phenotips.rest.PATCH;

import org.xwiki.rest.XWikiRestComponent;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Generic REST Interface for Web Resources.
 *
 * @version $Id$
 */
public interface ResourceRestEndpoint extends XWikiRestComponent
{
    String OFFSET = "offset";
    String LIMIT = "limit";
    String FILTER = "filter";
    String SORT = "sort";
    String IDS_ONLY = "idsOnly";

    String ID_PATH = "{id}";
    String ID = "id";

    /**
     * Creates a new resource and populates it with the contents of the given JSON.
     * @return a Response object containing the resource with its generated id
     */
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    Response create(String jsonString);

    /**
     * Returns a cohort as JSON.
     * @param resourceId the id of the resource
     * @return a Response object containing the resource
     */

    @Path(ID_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    Response get(@PathParam(ID) String resourceId);

    /**
     * Updates the resources identified by the given id with the contents of the given JSON.
     * @return a Response object containing the updated resource
     */
    @Path(ID_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PATCH
    Response update(@PathParam(ID) String resourceId, String jsonString);

    /**
     * Deletes the resource from the system.
     * @param resourceId the id of the resource
     * @return empty body
     */
    @Path(ID_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    @DELETE
    Response delete(
        @PathParam(ID) String resourceId);

    /**
     * Returns a list of resources.
     * @param offset the offset of the query
     * @param limit the size limit of the result set
     * @param filters the filter parameters
     * @param sorts the sort properties and orders
     * @param idsOnly if set to true the response should only include the resource ids, not the resources in
     *                their entirety
     * @return a Response object containing the JSON payload
     */
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @GET
    Response search(
        @QueryParam(FILTER) List<String> filters,
        @DefaultValue("doc.name") @QueryParam(SORT) List<String> sorts,
        @DefaultValue("0") @QueryParam(OFFSET) int offset,
        @DefaultValue("25") @QueryParam(LIMIT) int limit,
        @QueryParam(IDS_ONLY) boolean idsOnly
    );
}
