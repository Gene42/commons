package com.gene42.commons.rest;

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

import com.gene42.commons.data.ResourceFacade;
import com.gene42.commons.data.RestResource;
import com.gene42.commons.utils.exceptions.ServiceException;
import com.gene42.commons.utils.json.JsonApiBuilder;
import com.gene42.commons.utils.json.JsonApiResourceBuilder;
import com.gene42.commons.utils.web.WebUtils;

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

    public abstract String getResourceType();

    public abstract Type getResourceFacadeType();

    public ResourceFacade<T> getResourceFacade() throws ServiceException
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

            this.addResourceToBuilder(newResource, jsonBuilder);
        } catch (ServiceException e) {
            WebUtils.throwWebApplicationException(e, this.logger);
        } catch (JSONException e) {
            throw new WebApplicationException(
                WebUtils.getErrorResponse(Response.Status.BAD_REQUEST, e, "CHANGE ME", "n/a"));
        }

        return Response.ok(jsonBuilder.toString(), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response get(String resourceId)
    {
        JsonApiBuilder jsonBuilder = new JsonApiBuilder();

        try {
            T resource = this.getResourceFacade().get(resourceId);

            if (resource == null) {
                throw new ServiceException(ServiceException.Status.DATA_NOT_FOUND,
                    String.format("Could not find %s [%s]", this.getResourceType(), resourceId));
            }

            this.addResourceToBuilder(resource, jsonBuilder);
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
            this.addResourceToBuilder(resource, jsonBuilder);
        } catch (ServiceException e) {
            WebUtils.throwWebApplicationException(e, this.logger);
        } catch (JSONException e) {
            throw new WebApplicationException(
                WebUtils.getErrorResponse(Response.Status.BAD_REQUEST, e, "CHANGE ME", "n/a"));
        }

        return Response.ok(jsonBuilder.toString(), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response delete(String cohortId)
    {
        return null;
    }

    @Override
    public Response search(int offset, int limit, List<String> filter, List<String> sort)
    {
        return null;
    }

    private void addResourceToBuilder(T resource, JsonApiBuilder jsonBuilder) {
        jsonBuilder.addData(new JsonApiResourceBuilder(resource.getId(), resource.getResourceType())
            .putAttributes(resource.toJSONObject())
        );
    }
}
