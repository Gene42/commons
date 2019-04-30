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
import org.phenotips.data.api.internal.builder.DocumentSearchBuilder;

import org.xwiki.model.EntityType;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Builds an entity search request. This request can then be used with the entity search (as an input) to
 * perform the query.
 *
 * @version $Id$
 */
public class EntitySearchRequestBuilder
{
    private static final List<TableColumn> DEFAULT_COLUMNS = new LinkedList<>();

    static {
        Arrays.asList("doc.name", "doc.creator", "doc.creationDate", "doc.author", "doc.date").forEach(
            n -> DEFAULT_COLUMNS.add(new TableColumn(n, null, null, EntityType.DOCUMENT))
        );
    }

    private DocumentSearchBuilder queryBuilder;
    private List<TableColumn> tableColumns = new LinkedList<>(DEFAULT_COLUMNS);

    /**
     * Constructor.
     */
    public EntitySearchRequestBuilder()
    {
    }

    /**
     * Clears the column table.
     * @return this object
     */
    public EntitySearchRequestBuilder clearTableColumns()
    {
        this.tableColumns.clear();
        return this;
    }

    /**
     * Sets the DocumentSearchBuilder object. This object is required for the search, since it contains most of the
     * search request parameters.
     *
     * @param queryBuilder the DocumentSearchBuilder to use
     * @return this object
     */
    public EntitySearchRequestBuilder setDocumentSearchBuilder(DocumentSearchBuilder queryBuilder)
    {
        this.queryBuilder = queryBuilder;
        return this;
    }

    /**
     * Add a column to the the response, which corresponds to an object property in the resulting documents.
     *
     * @param colName the column name in the response object
     * @param propertyName the property name of of the object to grab the value from
     * @param className the name of the class of the object from which the value is extracted
     * @return this object
     */
    public EntitySearchRequestBuilder addObjectTableColumn(String colName, String propertyName, String className)
    {
        this.tableColumns.add(new TableColumn(colName, propertyName, className, EntityType.OBJECT));
        return this;
    }

    /**
     * Add a column to the the response, which corresponds to an object property in the resulting documents.
     * The column will have the same name as the given propertyName.
     *
     * @param propertyName the property name of of the object to grab the value from
     * @param className the name of the class of the object from which the value is extracted
     * @return this object
     */
    public EntitySearchRequestBuilder addObjectTableColumn(String propertyName, String className)
    {
        this.tableColumns.add(new TableColumn(propertyName, propertyName, className, EntityType.OBJECT));
        return this;
    }

    /**
     * Add a column to the the response, which corresponds to an document property in the resulting documents.
     *
     * @param colName the column name in the response object
     * @param propertyName the property name of of the object to grab the value from*
     * @return this object
     */
    public EntitySearchRequestBuilder addDocumentTableColumn(String colName, String propertyName)
    {
        this.tableColumns.add(new TableColumn(colName, propertyName, null, EntityType.DOCUMENT));
        return this;
    }

    /**
     * Builds the entity search request JSON.
     * @return a JSONObject containing the request.
     * @throws ServiceException if any issues occur during build.
     */
    public JSONObject build() throws ServiceException
    {
        if (this.queryBuilder == null) {
            throw new ServiceException("You must provide a DocumentSearchBuilder");
        }

        if (this.queryBuilder.getParent() != null) {
            throw new ServiceException("The DocumentSearchBuilder given is not the root query builder! You probably "
                + "forgot to call .back() on a sub query or expression");
        }

        if (CollectionUtils.isEmpty(this.tableColumns)) {
            throw new ServiceException("tableColumns cannot be empty");
        }

        JSONObject result = this.queryBuilder.build();
        JSONArray collList = new JSONArray();
        this.tableColumns.forEach(c -> collList.put(c.toJSONObject()));

        result.put(EntitySearch.Keys.COLUMN_LIST_KEY, collList);

        return result;
    }
}
