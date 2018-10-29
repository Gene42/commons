package org.phenotips.data.rest.internal;

import org.phenotips.data.api.internal.builder.DocumentSearchBuilder;
import org.phenotips.data.rest.LiveTableSearch;

import java.io.Closeable;
import java.io.IOException;

import org.apache.http.entity.ContentType;
import org.json.JSONObject;

import com.gene42.commons.utils.exceptions.ServiceException;
import com.gene42.commons.utils.web.HttpEndpoint;

/**
 * A helper class for performing entity search rest requests given a query in JSON format.
 *
 * @version $Id$
 */
public class EntitySearchRestEndpoint implements Closeable {

    private static final String RELATIVE_ENDPOINT = "rest" + LiveTableSearch.Paths.ROOT;

    private HttpEndpoint httpEndpoint;

    public EntitySearchRestEndpoint(HttpEndpoint httpEndpoint) {
        this.httpEndpoint = httpEndpoint;
    }

    /**
     * Perform an entity search and return the results.
     * @param queryBuilder the query builder object. Its build method will be called prior to the search.
     * @return a String representing the JSON output of the search result
     * @throws ServiceException if any error occur during the search
     */
    public String search(DocumentSearchBuilder queryBuilder) throws ServiceException {
        return this.search(queryBuilder.build());
    }

    /**
     * Perform an entity search and return the results.
     * @param query the query JSON object
     * @return a String representing the JSON output of the search result
     * @throws ServiceException if any error occur during the search
     */
    public String search(JSONObject query) throws ServiceException {
        return this.httpEndpoint.performPostRequest(RELATIVE_ENDPOINT, query.toString(), ContentType.APPLICATION_JSON);
    }

    @Override
    public void close() throws IOException {
        if (this.httpEndpoint != null) {
            this.httpEndpoint.close();
        }
    }
}
