/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal.builder;

import com.gene42.commons.utils.json.JSONafiable;

import org.phenotips.data.api.internal.PropertyName;
import org.phenotips.data.api.internal.SpaceAndClass;
import org.phenotips.data.api.internal.filter.AbstractFilter;

import org.json.JSONObject;

/**
 * Reference Value Container Class.
 *
 * @version $Id$
 */
public class ReferenceValue implements JSONafiable
{
    private String propertyName;
    private String spaceAndClass;
    private int level;

    /**
     * Getter for propertyName.
     *
     * @return propertyName
     */
    public String getPropertyName()
    {
        return this.propertyName;
    }

    /**
     * Setter for propertyName.
     *
     * @param propertyName propertyName to set
     * @return this object
     */
    public ReferenceValue setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;
        return this;
    }

    /**
     * Getter for spaceAndClass.
     *
     * @return spaceAndClass
     */
    public String getSpaceAndClass()
    {
        return this.spaceAndClass;
    }

    /**
     * Setter for spaceAndClass.
     *
     * @param spaceAndClass spaceAndClass to set
     * @return this object
     */
    public ReferenceValue setSpaceAndClass(String spaceAndClass)
    {
        this.spaceAndClass = spaceAndClass;
        return this;
    }

    /**
     * Getter for level.
     *
     * @return level
     */
    public int getLevel()
    {
        return this.level;
    }

    /**
     * Setter for level.
     *
     * @param level level to set
     * @return this object
     */
    public ReferenceValue setLevel(int level)
    {
        this.level = level;
        return this;
    }

    @Override
    public JSONObject toJSONObject()
    {
        JSONObject result = new JSONObject();
        result.put(SpaceAndClass.CLASS_KEY, this.spaceAndClass);
        result.put(PropertyName.PROPERTY_NAME_KEY, this.propertyName);
        result.put(AbstractFilter.PARENT_LEVEL_KEY, String.valueOf(this.level));
        return result;
    }
}
