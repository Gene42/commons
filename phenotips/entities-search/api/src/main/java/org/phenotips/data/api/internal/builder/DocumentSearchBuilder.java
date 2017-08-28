/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal.builder;

import org.phenotips.data.api.EntitySearch;
import org.phenotips.data.api.internal.DocumentQuery;
import org.phenotips.data.api.internal.QueryExpression;
import org.phenotips.data.api.internal.SpaceAndClass;
import org.phenotips.data.api.internal.filter.OrderFilter;
import org.phenotips.data.api.internal.filter.StringFilter;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
    private static final String PHENO_TIPS_COLLABORATOR_CLASS = "PhenoTips.CollaboratorClass";

    private JSONObject searchQuery = new JSONObject();

    private String docSpaceAndClass;

    private AbstractFilterBuilder<String> sortFilter;

    private List<DocumentSearchBuilder> queries = new LinkedList<>();

    private List<AbstractFilterBuilder> filters = new LinkedList<>();

    private DocumentSearchBuilder parent;

    private boolean negate;

    /**
     * Constructor.
     * @param docSpaceAndClass the document space and class (should be null if expression)
     */
    public DocumentSearchBuilder(String docSpaceAndClass)
    {
        this(null, docSpaceAndClass);
    }

    private DocumentSearchBuilder(DocumentSearchBuilder parent)
    {
        this.parent = parent;
    }

    private DocumentSearchBuilder(DocumentSearchBuilder parent, String docSpaceAndClass)
    {
        this.parent = parent;
        this.docSpaceAndClass = docSpaceAndClass;
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
     *  Adds the given DocumentSearchBuilder as a new subquery of this query.
     * @param subQuery the subQuery to add
     * @return the given subQuery
     */
    public DocumentSearchBuilder newSubQuery(DocumentSearchBuilder subQuery)
    {
        subQuery.parent = this;
        this.queries.add(subQuery);
        return subQuery;
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
        this.checkSortFilter();
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
        this.checkSortFilter();
        if (this.sortFilter != null) {
            this.sortFilter.setValue(OrderFilter.DESC).setPropertyName(propertyName);
        }
        return this;
    }

    /**
     * Sets the sorting order for the given property. If either the property name is null or the order is invalid,
     * this method will do nothing.
     * @param propertyName the name of the property to sort on
     * @param order the order in which to sort {asc,desc}.
     * @return this object
     */
    public DocumentSearchBuilder setSortOrder(String propertyName, String order)
    {
        if (!StringUtils.isBlank(propertyName)) {
            if (StringUtils.equalsIgnoreCase(OrderFilter.ASC, order)) {
                this.setSortAsc(propertyName);
            } else if (StringUtils.equalsIgnoreCase(OrderFilter.DESC, order)) {
                this.setSortDesc(propertyName);
            }
        }
        return this;
    }

    /**
     * Adds an inner query which checks for ownership (user), visibility (public, open) and collaborators
     * (both user and groups the user belongs to). Should be used on non Admin users.
     * They are checked with an OR operator, so only one of them is needed to match.
     *
     * @param fullUserName the full user name to use for checking ownership and collaboration status
     *                     ie: xwiki:XWiki.TestUser
     * @param fullUserGroupNames the full names of all groups the user with the given name belongs to
     *                           ie: xwiki:Groups.TestGroup
     * @return this object
     */
    public DocumentSearchBuilder onlyForUser(String fullUserName, Collection<String> fullUserGroupNames)
    {
        return this.onlyForUser(fullUserName, fullUserGroupNames, null);
    }

    /**
     * Adds an inner query which checks for ownership (user), visibility (public, open) and collaborators
     * (both user and groups the user belongs to). Should be used on non Admin users.
     * They are checked with an OR operator, so only one of them is needed to match.
     *
     * @param fullUserName the full user name to use for checking ownership and collaboration status
     *                     ie: xwiki:XWiki.TestUser
     * @param fullUserGroupNames the full names of all groups the user with the given name belongs to
     *                           ie: xwiki:Groups.TestGroup
     * @param accessRight the access right the user has over a document in order for it to be included in the search
     *                    result
     * @return this object
     */
    public DocumentSearchBuilder onlyForUser(String fullUserName, Collection<String> fullUserGroupNames,
        String accessRight)
    {
        DocumentSearchBuilder builder = this.newExpression()
            .setJoinModeToOr()
            .newStringFilter("owner")
            .setMatch(StringFilter.MATCH_EXACT)
            .setSpaceAndClass("PhenoTips.OwnerClass")
            .setValue(fullUserName).back()
            .newStringFilter("visibility")
            .setSpaceAndClass("PhenoTips.VisibilityClass")
            .setValues(Arrays.asList("public", "open")).back();

        if (accessRight != null) {
            builder = builder
                .newExpression()
                .setJoinModeToAnd()
                .newStringFilter("access")
                .setMatch(StringFilter.MATCH_EXACT)
                .setSpaceAndClass(PHENO_TIPS_COLLABORATOR_CLASS)
                .setValue(accessRight)
                .back();
        }

        builder.newStringFilter("collaborator")
            .setMatch(StringFilter.MATCH_EXACT)
            .setSpaceAndClass(PHENO_TIPS_COLLABORATOR_CLASS)
            .setValue(fullUserName)
            .addValues(fullUserGroupNames);

        return this;
    }

    /**
     * Getter for offset.
     *
     * @return offset
     */
    public int getOffset()
    {
        return this.searchQuery.optInt(EntitySearch.Keys.OFFSET_KEY, 0);
    }

    /**
     * Setter for offset.
     *
     * @param offset offset to set
     * @return this object
     */
    public DocumentSearchBuilder setOffset(int offset)
    {
        if (this.parent == null) {
            this.searchQuery.put(EntitySearch.Keys.OFFSET_KEY, offset);
        }
        return this;
    }

    /**
     * Getter for limit.
     *
     * @return limit
     */
    public int getLimit()
    {
        return this.searchQuery.optInt(EntitySearch.Keys.LIMIT_KEY, 25);
    }

    /**
     * Setter for limit.
     *
     * @param limit limit to set
     * @return this object
     */
    public DocumentSearchBuilder setLimit(int limit)
    {
        if (this.parent == null) {
            this.searchQuery.put(EntitySearch.Keys.LIMIT_KEY, limit);
        }
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
     * Creates + adds a new object filter builder and returns it.
     * @return a new ObjectFilterBuilder
     */
    public ObjectFilterBuilder newObjectFilter()
    {
        ObjectFilterBuilder filter = new ObjectFilterBuilder(this);
        this.filters.add(filter);
        return filter;
    }

    /**
     * Negates this query, ie 'exits' becomes 'not exists'. If you call this twice it will go back to original value.
     * !!true = true
     * @return this object
     */
    public DocumentSearchBuilder negate()
    {
        this.negate = !this.negate;
        return this;
    }

    /**
     * Getter for negate.
     *
     * @return negate
     */
    public boolean isNegated()
    {
        return this.negate;
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

        this.checkSortFilter();

        if (this.sortFilter != null) {
            this.searchQuery.put(EntitySearch.Keys.SORT_KEY, this.sortFilter.build());
        }

        this.searchQuery.put(SpaceAndClass.CLASS_KEY, this.docSpaceAndClass);

        if (this.negate) {
            this.searchQuery.put(QueryExpression.NEGATE_KEY, true);
        }

        return new JSONObject(this.searchQuery.toString());
    }

    private DocumentSearchBuilder checkSortFilter()
    {
        if (this.parent == null && this.sortFilter == null) {
            this.sortFilter = new StringFilterBuilder("doc.name", this)
                .setSpaceAndClass(this.docSpaceAndClass)
                .setType(OrderFilter.TYPE)
                .setValue(OrderFilter.ASC);
        }
        return this;
    }
}
