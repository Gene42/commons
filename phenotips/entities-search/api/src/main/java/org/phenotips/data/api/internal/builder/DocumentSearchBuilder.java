package org.phenotips.data.api.internal.builder;

import org.phenotips.data.api.DocumentSearch;
import org.phenotips.data.api.internal.DocumentQuery;
import org.phenotips.data.api.internal.SpaceAndClass;
import org.phenotips.data.api.internal.filter.OrderFilter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.Builder;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Builder Class for generating a DocumentSearch input JSONObject.
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

    /**
     * Constructor.
     * @param docSpaceAndClass the document space and class (should be null if expression)
     */
    public DocumentSearchBuilder(String docSpaceAndClass)
    {
        this(null, docSpaceAndClass, 0, 25);
    }

    /**
     * Constructor.
     * @param docSpaceAndClass the document space and class (should be null if expression)
     * @param offset the offset of the query
     * @param limit the limit of the query result
     */
    public DocumentSearchBuilder(String docSpaceAndClass, int offset, int limit)
    {
        this(null, docSpaceAndClass, offset, limit);
    }

    private DocumentSearchBuilder(DocumentSearchBuilder parent, String docSpaceAndClass)
    {
        this(parent, docSpaceAndClass, 0, 25);
    }

    private DocumentSearchBuilder(DocumentSearchBuilder parent)
    {
        this.parent = parent;
    }

    private DocumentSearchBuilder(DocumentSearchBuilder parent, String docSpaceAndClass, int offset, int limit)
    {
        this.parent = parent;

        this.docSpaceAndClass = docSpaceAndClass;

        if (this.parent == null) {
            this.setOffset(offset).setLimit(limit);
            this.sortFilter = new StringFilterBuilder("doc.name", this)
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

    /**
     * Creates + Adds a new expression for this query and returns it. An expression uses the same class
     * as the query builder, the only difference being that the class/docClass fields are not set.
     * @return the new expression
     */
    public DocumentSearchBuilder newExpression()
    {
        DocumentSearchBuilder newExpression = new DocumentSearchBuilder(this);
        this.queries.add(newExpression);
        return newExpression;
    }

    /**
     * Creates + Adds a new expression for this query and returns it.
     * @param docClass the document space and class of this new sub query
     * @return the new subquery
     */
    public DocumentSearchBuilder newSubQuery(String docClass)
    {
        DocumentSearchBuilder newSubQuery = new DocumentSearchBuilder(this, docClass);
        this.queries.add(newSubQuery);
        return newSubQuery;
    }

    /**
     * Returns the parent of this query builder. If it doesn't have one, it returns itself.
     * @return the DocumentSearchBuilder or itself
     */
    public DocumentSearchBuilder back()
    {
        if (this.parent == null) {
            return this;
        } else {
            return this.parent;
        }
    }

    /**
     * Sets the join mode to 'and'.
     * @return this object
     */
    public DocumentSearchBuilder setJoinModeToAnd()
    {
        this.searchQuery.put(DocumentQuery.JOIN_MODE_KEY, "and");
        return this;
    }

    /**
     * Sets the join mode to 'or'.
     * @return this object
     */
    public DocumentSearchBuilder setJoinModeToOr()
    {
        this.searchQuery.put(DocumentQuery.JOIN_MODE_KEY, "or");
        return this;
    }

    /**
     * Sets the sort order to 'asc' for the given property.
     * @param propertyName the name of the property to sort on
     * @return this object
     */
    public DocumentSearchBuilder setSortAsc(String propertyName)
    {
        if (this.sortFilter != null) {
            this.sortFilter.setValue(OrderFilter.ASC).setPropertyName(propertyName);
        }
        return this;
    }

    /**
     * Sets the sort order to 'desc' for the given property.
     * @param propertyName the name of the property to sort on
     * @return this object
     */
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

    /**
     * Creates + adds a new string filter builder and returns it.
     * @param propertyName the name of the property
     * @return a new StringFilterBuilder
     */
    public StringFilterBuilder newStringFilter(String propertyName)
    {
        StringFilterBuilder filter = new StringFilterBuilder(propertyName, this);
        this.filters.add(filter);
        return filter;
    }

    /**
     * Creates + adds a new number filter builder and returns it.
     * @param propertyName the name of the property
     * @return a new NumberFilterBuilder
     */
    public NumberFilterBuilder newNumberFilter(String propertyName)
    {
        NumberFilterBuilder filter = new NumberFilterBuilder(propertyName, this);
        this.filters.add(filter);
        return filter;
    }

    /**
     * Getter for parent.
     *
     * @return parent
     */
    public DocumentSearchBuilder getParent()
    {
        return this.parent;
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

        this.searchQuery.put(SpaceAndClass.CLASS_KEY, this.docSpaceAndClass);

        return new JSONObject(this.searchQuery.toString());
    }
}
