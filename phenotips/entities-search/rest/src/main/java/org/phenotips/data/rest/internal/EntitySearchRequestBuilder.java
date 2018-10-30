package org.phenotips.data.rest.internal;

import org.phenotips.data.api.EntitySearch;
import org.phenotips.data.api.internal.builder.DocumentSearchBuilder;

import org.xwiki.model.EntityType;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.gene42.commons.utils.exceptions.ServiceException;

/**
 * DESCRIPTION.
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

    public EntitySearchRequestBuilder() {
    }

    public EntitySearchRequestBuilder clearTableColumns() {
        this.tableColumns.clear();
        return this;
    }

    public EntitySearchRequestBuilder setDocumentSearchBuilder(DocumentSearchBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
        return this;
    }

    public EntitySearchRequestBuilder addObjectTableColumn(String colName, String propertyName, String className) {
        this.tableColumns.add(new TableColumn(colName, propertyName, className, EntityType.OBJECT));
        return this;
    }

    public EntitySearchRequestBuilder addObjectTableColumn(String propertyName, String className) {
        this.tableColumns.add(new TableColumn(propertyName, propertyName, className, EntityType.OBJECT));
        return this;
    }

    public EntitySearchRequestBuilder addDocumentTableColumn(String colName, String propertyName) {
        this.tableColumns.add(new TableColumn(colName, propertyName, null, EntityType.DOCUMENT));
        return this;
    }

    public JSONObject build() throws ServiceException {
        if (this.queryBuilder == null) {
            throw new ServiceException("You must provide a DocumentSearchBuilder");
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
