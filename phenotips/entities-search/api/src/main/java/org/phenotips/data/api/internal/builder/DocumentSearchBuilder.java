package org.phenotips.data.api.internal.builder;

import org.phenotips.data.api.DocumentSearch;
import org.phenotips.data.api.internal.DocumentQuery;
import org.phenotips.data.api.internal.filter.OrderFilter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.Builder;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public class DocumentSearchBuilder implements Builder<JSONObject>
{
    private JSONObject searchQuery = new JSONObject();

    private String docSpaceAndClass;

    private AbstractFilterBuilder<String> sortFilter;

    private List<DocumentSearchBuilder> queries = new ArrayList<>();

    private List<AbstractFilterBuilder> filters = new ArrayList<>();

    private DocumentSearchBuilder parent;

    public DocumentSearchBuilder(String docSpaceAndClass)
    {
        this(null, docSpaceAndClass, 0, 25);
    }

    public DocumentSearchBuilder(String docSpaceAndClass, int offset, int limit)
    {
        this(null, docSpaceAndClass, offset, limit);
    }

    private DocumentSearchBuilder(DocumentSearchBuilder parent, String docSpaceAndClass)
    {
        this(parent, docSpaceAndClass, 0, 25);
    }

    private DocumentSearchBuilder(DocumentSearchBuilder parent) {
        this.parent = parent;
    }

    private DocumentSearchBuilder(DocumentSearchBuilder parent, String docSpaceAndClass, int offset, int limit)
    {
        this.parent = parent;

        this.docSpaceAndClass = docSpaceAndClass;

        if (this.parent == null) {
            this.setOffset(offset).setLimit(limit);
            this.sortFilter = new StringFilterBuilder(this)
                .setPropertyName("doc.name")
                .setSpaceAndClass(this.docSpaceAndClass)
                .setType(OrderFilter.TYPE)
                .setValue(OrderFilter.ASC);
        }

        this.setJoinModeToAnd();
    }

    /**
     * Getter for docSpaceAndClass.
     *
     * @return docSpaceAndClass
     */
    public String getDocSpaceAndClass()
    {
        return this.docSpaceAndClass;
    }

    public DocumentSearchBuilder newExpression()
    {
        DocumentSearchBuilder newExpression = new DocumentSearchBuilder(this);
        this.queries.add(newExpression);
        return newExpression;
    }

    public DocumentSearchBuilder newSubQuery(String docClass)
    {
        DocumentSearchBuilder newExpression = new DocumentSearchBuilder(this);
        this.queries.add(newExpression);
        return newExpression;
    }

    public DocumentSearchBuilder back()
    {
        if (this.parent == null) {
            return this;
        } else {
            return this.parent;
        }
    }

    public DocumentSearchBuilder setJoinModeToAnd()
    {
        this.searchQuery.put(DocumentQuery.JOIN_MODE_KEY, "and");
        return this;
    }

    public DocumentSearchBuilder setJoinModeToOr()
    {
        this.searchQuery.put(DocumentQuery.JOIN_MODE_KEY, "or");
        return this;
    }

    public DocumentSearchBuilder setSortAsc(String propertyName)
    {
        if (this.sortFilter != null) {
            this.sortFilter.setValue(OrderFilter.ASC).setPropertyName(propertyName);
        }
        return this;
    }

    public DocumentSearchBuilder setSortDesc(String propertyName)
    {
        if (this.sortFilter != null) {
            this.sortFilter.setValue(OrderFilter.DESC).setPropertyName(propertyName);
        }
        return this;
    }

    /**
     * Getter for offset.
     *
     * @return offset
     */
    public int getOffset()
    {
        return this.searchQuery.optInt(DocumentSearch.OFFSET_KEY, 0);
    }

    /**
     * Setter for offset.
     *
     * @param offset offset to set
     * @return this object
     */
    public DocumentSearchBuilder setOffset(int offset)
    {
        this.searchQuery.put(DocumentSearch.OFFSET_KEY, offset);
        return this;
    }

    /**
     * Getter for limit.
     *
     * @return limit
     */
    public int getLimit()
    {
        return this.searchQuery.optInt(DocumentSearch.LIMIT_KEY, 25);
    }

    /**
     * Setter for limit.
     *
     * @param limit limit to set
     * @return this object
     */
    public DocumentSearchBuilder setLimit(int limit)
    {
        this.searchQuery.put(DocumentSearch.LIMIT_KEY, limit);
        return this;
    }


    public StringFilterBuilder newStringFilter()
    {
        StringFilterBuilder filter = new StringFilterBuilder(this);
        this.filters.add(filter);
        return filter;
    }

    public NumberFilterBuilder newNumberFilter()
    {
        NumberFilterBuilder filter = new NumberFilterBuilder(this);
        this.filters.add(filter);
        return filter;
    }

    /**
     * Getter for joinMode.
     *
     * @return joinMode
     */
    public String getJoinMode()
    {
        return this.searchQuery.optString(DocumentQuery.JOIN_MODE_KEY, "and");
    }

    @Override
    public JSONObject build()
    {
        if (CollectionUtils.isNotEmpty(this.filters)) {
            JSONArray filtersArray = new JSONArray();
            this.searchQuery.put(DocumentQuery.FILTERS_KEY, filtersArray);

            for (AbstractFilterBuilder filterBuilder : this.filters) {
                filtersArray.put(filterBuilder.build());
            }
        }

        if (CollectionUtils.isNotEmpty(this.queries)) {
            JSONArray queriesArray = new JSONArray();
            this.searchQuery.put(DocumentQuery.QUERIES_KEY, queriesArray);

            for (DocumentSearchBuilder subQueryBuilder : this.queries) {
                queriesArray.put(subQueryBuilder.build());
            }
        }

        if (this.sortFilter != null) {
            this.searchQuery.put(DocumentSearch.ORDER_KEY, this.sortFilter.build());
        }

        return new JSONObject(this.searchQuery.toString());
    }
}
