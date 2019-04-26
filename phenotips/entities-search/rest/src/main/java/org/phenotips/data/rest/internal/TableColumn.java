/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.rest.internal;

import com.gene42.commons.utils.json.JSONTools;
import com.gene42.commons.utils.json.JSONafiable;

import org.phenotips.data.api.EntitySearch;
import org.phenotips.data.api.internal.PropertyName;

import org.xwiki.model.EntityType;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A container class for holding defining information about a column.
 *
 * @version $Id$
 */
public class TableColumn implements JSONafiable
{
    /** Key. */
    public static final String TYPE_KEY = "type";

    /** Key. */
    public static final String CLASS_KEY = "class";

    /** Key. */
    public static final String PROPERTY_NAME_KEY = "propertyName";

    /** Key. */
    public static final String COLUMN_NAME_KEY = "colName";


    private EntityType type;
    private String colName;
    private String className;
    private String propertyName;
    private List<JSONObject> filters;

    /**
     * Constructor.
     */
    public TableColumn()
    {
    }

    /**
     * Constructor.
     * @param colName the column name
     * @param propertyName the name of the property
     * @param className the name of the class of the object
     * @param type the type of the entity : doc, XwikiObject
     */
    public TableColumn(String colName, String propertyName, String className, EntityType type)
    {

        if (type == null) {
            throw new IllegalArgumentException("No type provided");
        }

        if (colName == null) {
            throw new IllegalArgumentException("No colName provided");
        }

        this.colName = colName;
        this.type = type;
        boolean isDoc = EntityType.DOCUMENT.equals(type);

        if (!isDoc && className == null) {
            throw new IllegalArgumentException("No className provided");
        }

        this.className = className;

        if (StringUtils.isBlank(propertyName)) {
            this.propertyName = this.colName;
        } else {
            this.propertyName = propertyName;
        }
    }

    /**
     * Constructor.
     * @param obj the input object to use to populate this object
     * @param query the search query json. Used to retrieve filters associated with this column
     * @return this
     */
    public TableColumn populate(JSONObject obj, JSONObject query)
    {
        this.type = EntityType.valueOf(StringUtils.upperCase(getProperty(obj, TYPE_KEY, false)));

        this.className = getProperty(obj, CLASS_KEY, EntityType.DOCUMENT.equals(this.type));

        this.colName = getProperty(obj, COLUMN_NAME_KEY, false);

        this.propertyName = getProperty(obj, PROPERTY_NAME_KEY, true);

        if (StringUtils.isBlank(this.propertyName)) {
            this.propertyName = this.colName;
        }

        this.filters = this.getFiltersAssociatedWithColumn(query);

        return this;
    }

    /**
     * Getter for filters.
     *
     * @return filters
     */
    public List<JSONObject> getFilters() {
        return this.filters;
    }

    @Override
    public JSONObject toJSONObject()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TYPE_KEY, this.type);
        jsonObject.put(CLASS_KEY, this.className);
        jsonObject.put(COLUMN_NAME_KEY, this.colName);
        jsonObject.put(PROPERTY_NAME_KEY, this.propertyName);
        return jsonObject;
    }

    /**
     * Getter for type.
     *
     * @return type
     */
    public EntityType getType()
    {
        return this.type;
    }

    /**
     * Getter for colName.
     *
     * @return colName
     */
    public String getColName()
    {
        return this.colName;
    }

    /**
     * Getter for className.
     *
     * @return className
     */
    public String getClassName()
    {
        return this.className;
    }

    /**
     * Getter for propertyName.
     *
     * @return propertyName
     */
    public String getPropertyName()
    {
        return this.propertyName;
    }

    private static String getProperty(JSONObject obj, String key, boolean canBeBlank)
    {
        String propStr = obj.optString(key);
        if (StringUtils.isBlank(propStr) && !canBeBlank) {
            throw new IllegalArgumentException(String.format("No %1$s provided", key));
        }
        return propStr;
    }

    private List<JSONObject> getFiltersAssociatedWithColumn(JSONObject query) {

        List<JSONObject> result = new LinkedList<>();

        JSONArray array = JSONTools.getJSONArray(query, EntitySearch.Keys.FILTERS_KEY);

        for (Object obj : array) {
            if (!(obj instanceof JSONObject)) {
                continue;
            }

            JSONObject filter = (JSONObject) obj;

            if (org.xwiki.text.StringUtils.equals(this.getPropertyName(),
                filter.optString(PropertyName.PROPERTY_NAME_KEY, null))) {

                result.add(filter);
            }
        }

        return result;
    }
}
