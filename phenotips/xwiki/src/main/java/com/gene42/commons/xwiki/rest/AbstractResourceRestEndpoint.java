package com.gene42.commons.xwiki.rest;

import org.phenotips.data.api.EntitySearch;
import org.phenotips.data.api.EntitySearchResult;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;

import java.lang.reflect.Type;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gene42.commons.utils.exceptions.ServiceException;
import com.gene42.commons.utils.json.JsonApiBuilder;
import com.gene42.commons.utils.json.JsonApiErrorBuilder;
import com.gene42.commons.utils.json.JsonApiResourceBuilder;
import com.gene42.commons.utils.web.WebUtils;
import com.gene42.commons.xwiki.data.ResourceFacade;
import com.gene42.commons.xwiki.data.ResourceOperation;
import com.gene42.commons.xwiki.data.RestResource;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public abstract class AbstractResourceRestEndpoint<T extends RestResource> implements ResourceRestEndpoint
{
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static final NoAccessResource NO_ACCESS_RESOURCE = new NoAccessResource();

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
            this.getResourceFacade().hasAccess(ResourceOperation.CREATE, (T) null, true);
            T newResource = this.getResourceFacade().create(new JSONObject(jsonString));
            this.addResourceToBuilder("n/a", newResource, jsonBuilder);
        } catch (ServiceException e) {
            WebUtils.throwWebApplicationException(e, this.logger);
        } catch (JSONException e) {
            throw new WebApplicationException(
                WebUtils.getErrorResponse(Response.Status.BAD_REQUEST, e, this.getRootPath(), "n/a"));
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
            WebUtils.throwWebApplicationException(e, this.logger);
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
            WebUtils.throwWebApplicationException(e, this.logger);
        } catch (JSONException e) {
            throw new WebApplicationException(
                WebUtils.getErrorResponse(Response.Status.BAD_REQUEST, e, this.getResourceUri(resourceId), "n/a"));
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
            WebUtils.throwWebApplicationException(e, this.logger);
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
            WebUtils.throwWebApplicationException(e, this.logger);
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
        return this.getRootPath() + "/" + resourceId;
    }
}
