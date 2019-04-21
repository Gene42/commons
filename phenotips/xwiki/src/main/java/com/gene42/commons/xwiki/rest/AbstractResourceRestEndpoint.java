/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.xwiki.rest;

import com.gene42.commons.utils.exceptions.ServiceException;
import com.gene42.commons.utils.json.JsonApiBuilder;
import com.gene42.commons.utils.json.JsonApiErrorBuilder;
import com.gene42.commons.utils.json.JsonApiResourceBuilder;
import com.gene42.commons.utils.web.WebUtils;
import com.gene42.commons.xwiki.data.ResourceFacade;
import com.gene42.commons.xwiki.data.ResourceOperation;
import com.gene42.commons.xwiki.data.RestResource;

import org.phenotips.data.api.EntitySearch;
import org.phenotips.data.api.EntitySearchResult;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;

import java.lang.reflect.Type;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DESCRIPTION.
 *
 * @param <T> entity extending the RestResource interface
 * @version $Id$
 */
public abstract class AbstractResourceRestEndpoint<T extends RestResource> implements ResourceRestEndpoint
{
    public static final String N_A = "n/a";

    protected static final NoAccessResource NO_ACCESS_RESOURCE = new NoAccessResource();

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    private ComponentManager componentManager;

    @Inject
    private EntitySearch<DocumentReference> documentSearch;

    /**
     * Return the type of REST resource this endpoint provides. This is the type to be used in the JSONAPI
     * data type field.
     * @return the type
     */
    public abstract String getResourceType();

    /**
     * Return the Type (used for reflection) of the facade responsible with dealing with this REST resource
     * at the business level. This will be used to instantiate the facade.
     * @return the Type
     */
    public abstract Type getResourceFacadeType();

    /**
     * Return the root path of the REST endpoint for this particular resource. If the root REST path is
     * '/rest' for example, the root resource path be as follows `/rest/{rootPath}`
     * @return the resource root path
     */
    public abstract String getResourceRootPath();

    /**
     * Uses the getResourceFacadeType() in order to instantiate the ResourceFacade needed for this resource.
     * @return a ResourceFacade of given Type
     * @throws ServiceException if any issues occur during instantiation
     */
    protected ResourceFacade<T> getResourceFacade() throws ServiceException
    {
        try {
            return this.componentManager.getInstance(this.getResourceFacadeType());
        } catch (ComponentLookupException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public Response create(String jsonString)
    {
        JsonApiBuilder jsonBuilder = new JsonApiBuilder();

        try {
            this.getResourceFacade().hasAccess(ResourceOperation.CREATE, (T) null, true);
            T newResource = this.getResourceFacade().create(new JSONObject(jsonString));
            this.addResourceToBuilder(N_A, newResource, jsonBuilder);
        } catch (ServiceException e) {
            return WebUtils.getErrorResponse(e, this.logger);
        } catch (JSONException e) {
            return WebUtils.getErrorResponse(Response.Status.BAD_REQUEST, e, this.getResourceRootPath(), N_A);
        }

        return this.getOkResponse(jsonBuilder);
    }

    @Override
    public Response get(String resourceId)
    {
        JsonApiBuilder jsonBuilder = new JsonApiBuilder();

        try {
            T resource = this.getResourceFacade().get(resourceId, true);
            this.getResourceFacade().hasAccess(ResourceOperation.GET, resource, true);
            this.addResourceToBuilder(resourceId, resource, jsonBuilder);
        } catch (ServiceException e) {
            return WebUtils.getErrorResponse(e, this.logger);
        }

        return this.getOkResponse(jsonBuilder);
    }

    @Override
    public Response update(String resourceId, String jsonString)
    {
        JsonApiBuilder jsonBuilder = new JsonApiBuilder();

        try {
            T resource = this.getResourceFacade().update(resourceId, new JSONObject(jsonString), true);
            this.getResourceFacade().hasAccess(ResourceOperation.UPDATE, resource, true);
            this.addResourceToBuilder(resourceId, resource, jsonBuilder);
        } catch (ServiceException e) {
            return WebUtils.getErrorResponse(e, this.logger);
        } catch (JSONException e) {
            return WebUtils.getErrorResponse(Response.Status.BAD_REQUEST, e, this.getResourceUri(resourceId), N_A);
        }

        return this.getOkResponse(jsonBuilder);
    }

    @Override
    public Response delete(String resourceId)
    {
        JsonApiBuilder jsonBuilder = new JsonApiBuilder();

        try {
            this.getResourceFacade().hasAccess(ResourceOperation.DELETE, resourceId, true);
            T resource = this.getResourceFacade().delete(resourceId, true);
            this.addResourceToBuilder(resourceId, resource, jsonBuilder);
        } catch (ServiceException e) {
            return WebUtils.getErrorResponse(e, this.logger);
        }

        return this.getOkResponse(jsonBuilder);
    }

    @Override
    public Response search(List<String> filters, List<String> sorts, int offset, int limit, boolean idsOnly)
    {
        JsonApiBuilder jsonBuilder = new JsonApiBuilder();

        try {
            this.getResourceFacade().hasAccess(ResourceOperation.SEARCH, (T) null, true);
            EntitySearchResult<String> searchResult = this.getResourceFacade().search(filters, sorts, offset, limit);

            for (String resourceId : searchResult.getItems()) {
                if (idsOnly) {
                    jsonBuilder.addData(new JsonApiResourceBuilder(resourceId, this.getResourceType()));
                } else {
                    T resource = this.getResourceFacade().get(resourceId, false);
                    if (this.getResourceFacade().hasAccess(ResourceOperation.GET, resource, false)) {
                        this.addResourceToBuilder(resourceId, resource, jsonBuilder);
                    } else {
                        this.addResourceToBuilder(resourceId, NO_ACCESS_RESOURCE, jsonBuilder);
                    }
                }
            }

            jsonBuilder.putMeta(WebUtils.OFFSET_KEY, offset)
                    .putMeta(WebUtils.LIMIT_KEY, limit)
                    .putMeta(WebUtils.TOTAL_KEY, searchResult.getTotalRows())
                    .putMeta(WebUtils.RETURNED_KEY, searchResult.getItems().size());

        } catch (ServiceException e) {
            return WebUtils.getErrorResponse(e, this.logger);
        }

        return this.getOkResponse(jsonBuilder);
    }

    protected Response getOkResponse(JsonApiBuilder jsonBuilder) {
        return Response.ok(jsonBuilder.toString(), MediaType.APPLICATION_JSON_TYPE).build();
    }

    protected <M extends RestResource> void addResourceToBuilder(String resourceId, M resource,
        JsonApiBuilder jsonBuilder) throws ServiceException {

        if (resource == null) {
            jsonBuilder.addError(new JsonApiErrorBuilder()
                    .setDetail(String.format("Could not find [%s]", resourceId)));
        } else {
            jsonBuilder.addData(new JsonApiResourceBuilder(resource.getId(), resource.getResourceType())
                    .putAttributes(resource.toJSONObject())
            );
        }
    }

    @ThreadSafe
    protected static class NoAccessResource implements RestResource {
        @Override
        public String getId()
        {
            return "-";
        }

        @Override
        public String getResourceType()
        {
            return "no-access";
        }

        @Override
        public JSONObject toJSONObject()
        {
            return new JSONObject();
        }
    }

    private String getResourceUri(String resourceId) {
        return this.getResourceRootPath() + "/" + resourceId;
    }
}
