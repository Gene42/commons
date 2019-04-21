/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.rest.internal;

import com.gene42.commons.utils.exceptions.ServiceException;
import com.gene42.commons.utils.json.JSONTools;
import com.gene42.commons.utils.web.HttpEndpoint;

import org.phenotips.data.api.EntitySearch;
import org.phenotips.data.api.EntitySearchResult;
import org.phenotips.data.rest.LiveTableSearch;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.http.entity.ContentType;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A helper class for performing entity search rest requests given a query in JSON format.
 *
 * @version $Id$
 */
public class EntitySearchRestEndpoint implements Closeable, EntitySearch<JSONObject>
{

    private static final String RELATIVE_ENDPOINT = "rest" + LiveTableSearch.Paths.ROOT;

    private HttpEndpoint httpEndpoint;

    /**
     * Constructor.
     * @param httpEndpoint HttpEndpoint to use for the connection
     */
    public EntitySearchRestEndpoint(HttpEndpoint httpEndpoint)
    {
        this.httpEndpoint = httpEndpoint;
    }

    /**
     * Perform an entity search and return the results.
     * @param queryBuilder the query builder object. Its build method will be called prior to the search.
     * @return a String representing the JSON output of the search result
     * @throws ServiceException if any error occur during the search
     */
    @NotNull
    public EntitySearchResult<JSONObject> search(EntitySearchRequestBuilder queryBuilder) throws ServiceException
    {
        return this.search(queryBuilder.build());
    }

    /**
     * Perform an entity search and return the results.
     * @param query the query JSON object
     * @return a String representing the JSON output of the search result
     * @throws ServiceException if any error occur during the search
     */
    @NotNull
    public EntitySearchResult<JSONObject> search(JSONObject query) throws ServiceException
    {
        String resultStr = this.httpEndpoint
            .performPostRequest(RELATIVE_ENDPOINT, query.toString(), ContentType.APPLICATION_JSON);

        JSONObject resultObj = new JSONObject(resultStr);

        return new EntitySearchResult<JSONObject>()
            .setOffset(resultObj.optInt(LiveTableSearch.Keys.OFFSET))
            .setTotalRows(resultObj.optInt(LiveTableSearch.Keys.TOTAL_ROWS))
            .setItems(JSONTools.jsonArrayToList(Optional
                .ofNullable(resultObj.optJSONArray(LiveTableSearch.Keys.ROWS))
                .orElse(new JSONArray()), true)
                .stream()
                .filter(o -> o instanceof JSONObject)
                .map(o -> (JSONObject) o).collect(Collectors.toList()));
    }

    /**
     * Getter for httpEndpoint.
     *
     * @return httpEndpoint
     */
    public HttpEndpoint getHttpEndpoint()
    {
        return this.httpEndpoint;
    }

    @Override
    public void close() throws IOException
    {
        if (this.httpEndpoint != null) {
            this.httpEndpoint.close();
        }
    }
}
