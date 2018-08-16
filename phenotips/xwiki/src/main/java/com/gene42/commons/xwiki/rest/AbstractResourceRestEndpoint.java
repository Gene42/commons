package com.gene42.commons.xwiki.rest;

import com.gene42.commons.utils.json.JsonApiErrorBuilder;
import org.phenotips.data.api.EntitySearch;
import org.phenotips.data.api.EntitySearchResult;
import org.phenotips.data.api.internal.builder.DocumentSearchBuilder;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.gene42.commons.xwiki.data.ResourceFacade;
import com.gene42.commons.xwiki.data.RestResource;
import com.gene42.commons.utils.exceptions.ServiceException;
import com.gene42.commons.utils.json.JsonApiBuilder;
import com.gene42.commons.utils.json.JsonApiResourceBuilder;
import com.gene42.commons.utils.web.WebUtils;
import org.xwiki.model.reference.DocumentReference;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public abstract class AbstractResourceRestEndpoint<T extends RestResource> implements ResourceRestEndpoint
{
    @Inject
    private Logger logger;

    @Inject
    private ComponentManager componentManager;

    @Inject
    private EntitySearch<DocumentReference> documentSearch;

    public abstract String getResourceType();

    public abstract Type getResourceFacadeType();

    public abstract String getRootPath();

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
            T newResource = this.getResourceFacade().create(new JSONObject(jsonString));

            this.addResourceToBuilder("n/a", newResource, jsonBuilder);
        } catch (ServiceException e) {
            WebUtils.throwWebApplicationException(e, this.logger);
        } catch (JSONException e) {
            throw new WebApplicationException(
                WebUtils.getErrorResponse(Response.Status.BAD_REQUEST, e, this.getRootPath(), "n/a"));
        }

        return Response.ok(jsonBuilder.toString(), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response get(String resourceId)
    {
        JsonApiBuilder jsonBuilder = new JsonApiBuilder();

        try {
            T resource = this.getResourceFacade().get(resourceId);
            this.checkResource(resourceId, resource);
            this.addResourceToBuilder(resourceId, resource, jsonBuilder);
        } catch (ServiceException e) {
            WebUtils.throwWebApplicationException(e, this.logger);
        }

        return Response.ok(jsonBuilder.toString(), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response update(String resourceId, String jsonString)
    {
        JsonApiBuilder jsonBuilder = new JsonApiBuilder();

        try {
            T resource = this.getResourceFacade().update(resourceId, new JSONObject(jsonString));
            this.addResourceToBuilder(resourceId, resource, jsonBuilder);
        } catch (ServiceException e) {
            WebUtils.throwWebApplicationException(e, this.logger);
        } catch (JSONException e) {
            throw new WebApplicationException(
                WebUtils.getErrorResponse(Response.Status.BAD_REQUEST, e, this.getResourceUri(resourceId), "n/a"));
        }

        return Response.ok(jsonBuilder.toString(), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response delete(String resourceId)
    {
        JsonApiBuilder jsonBuilder = new JsonApiBuilder();

        try {
            T resource = this.getResourceFacade().delete(resourceId);
            this.checkResource(resourceId, resource);
            this.addResourceToBuilder(resourceId, resource, jsonBuilder);
        } catch (ServiceException e) {
            WebUtils.throwWebApplicationException(e, this.logger);
        }

        return Response.ok(jsonBuilder.toString(), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response search(List<String> filters, List<String> sorts, int offset, int limit, boolean idsOnly)
    {
        JsonApiBuilder jsonBuilder = new JsonApiBuilder();

        try {
            EntitySearchResult<String> searchResult = this.getResourceFacade().search(filters, sorts, offset, limit);

            for (String resourceId : searchResult.getItems()) {
                if (idsOnly) {
                    jsonBuilder.addData(new JsonApiResourceBuilder(resourceId, this.getResourceType()));
                } else {
                    this.addResourceToBuilder(resourceId, this.getResourceFacade().get(resourceId), jsonBuilder);
                }
            }

            jsonBuilder.putMeta(WebUtils.OFFSET_KEY, offset)
                    .putMeta(WebUtils.LIMIT_KEY, limit)
                    .putMeta(WebUtils.TOTAL_KEY, searchResult.getTotalRows())
                    .putMeta(WebUtils.RETURNED_KEY, searchResult.getItems().size());

        } catch (ServiceException e) {
            WebUtils.throwWebApplicationException(e, this.logger);
        }

        return Response.ok(jsonBuilder.toString(), MediaType.APPLICATION_JSON_TYPE).build();
    }

    private void checkResource(String resourceId, T resource) throws ServiceException {
        if (resource == null) {
            throw new ServiceException(ServiceException.Status.DATA_NOT_FOUND,
                    String.format("Could not find %s [%s]", this.getResourceType(), resourceId));
        }
    }

    private void addResourceToBuilder(String resourceId, T resource, JsonApiBuilder jsonBuilder) {

        if (resource == null) {
            jsonBuilder.addError(new JsonApiErrorBuilder()
                    .setDetail(String.format("Could not find [%s]", resourceId)));
        } else {
            jsonBuilder.addData(new JsonApiResourceBuilder(resource.getId(), resource.getResourceType())
                    .putAttributes(resource.toJSONObject())
            );
        }
    }

    private String getResourceUri(String resourceId) {
        return this.getRootPath() + "/" + resourceId;
    }
}