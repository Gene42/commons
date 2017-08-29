/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal.builder;

import org.phenotips.data.api.internal.PropertyName;
import org.phenotips.data.api.internal.SpaceAndClass;
import org.phenotips.data.api.internal.filter.AbstractFilter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.Builder;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Builder Class for generating a filter JSONObject to be used in a DocumentSearch input JSONObject.
 *
 * @param <T> the type of filter
 * @version $Id$
 */
@SuppressWarnings({"checkstyle:npathcomplexity"})
public abstract class AbstractFilterBuilder<T> implements Builder<JSONObject>
{
    private DocumentSearchBuilder parent;

    private String docSpaceAndClass;
    private String spaceAndClass;
    private String propertyName;
    private String type;
    private T minValue;
    private T maxValue;
    private String minKey;
    private String maxKey;
    private List<T> values = new LinkedList<>();
    private List<ReferenceValue> refValues = new LinkedList<>();

    /**
     * Constructor.
     * @param parent the parent DocumentSearchBuilder of this filter
     */
    public AbstractFilterBuilder(DocumentSearchBuilder parent)
    {
        this(null, parent);
    }

    /**
     * Constructor.
     * @param propertyName the name of the property
     * @param parent the parent DocumentSearchBuilder of this filter
     */
    public AbstractFilterBuilder(String propertyName, DocumentSearchBuilder parent)
    {
        this.parent = Objects.requireNonNull(parent);

        DocumentSearchBuilder currentParent = this.parent;

        String parentSpaceAndClass = null;
        while (currentParent != null && parentSpaceAndClass == null) {
            parentSpaceAndClass = currentParent.getDocSpaceAndClass();
            currentParent = currentParent.getParent();
        }

        this.setDocSpaceAndClass(parentSpaceAndClass);
        this.setSpaceAndClass(this.getDocSpaceAndClass());
        this.setPropertyName(propertyName);
    }

    /**
     * Getter for values.
     *
     * @return values
     */
    public List<T> getValues()
    {
        return this.values;
    }

    /**
     * Setter for values.
     *
     * @param values values to set
     * @return this object
     */
    public AbstractFilterBuilder<T> setValues(List<T> values)
    {
        this.values = values;
        return this;
    }

    /**
     * Adds all values to the value list.
     *
     * @param values values to set
     * @return this object
     */
    public AbstractFilterBuilder<T> addValues(Collection<T> values)
    {
        if (CollectionUtils.isNotEmpty(values)) {
            this.values.addAll(values);
        }
        return this;
    }

    /**
     * Adds the value to the value list.
     *
     * @param value value to add
     * @return this object
     */
    public AbstractFilterBuilder<T> addValue(T value)
    {
        this.values.add(value);
        return this;
    }

    /**
     * Replaces the current list of values with the given one.
     *
     * @param value value to add
     * @return this object
     */
    public AbstractFilterBuilder<T> setValue(T value)
    {
        if (CollectionUtils.isNotEmpty(this.values)) {
            this.values = new LinkedList<>();
        }

        return this.addValue(value);
    }

    /**
     * Setter for refValues.
     *
     * @param refValues refValues to set
     * @return this object
     */
    public AbstractFilterBuilder<T> setReferenceValues(List<ReferenceValue> refValues)
    {
        this.refValues = refValues;
        return this;
    }

    /**
     * Adds all given refValues to the refValues list.
     *
     * @param refValues refValues to set
     * @return this object
     */
    public AbstractFilterBuilder<T> addReferenceValues(Collection<ReferenceValue> refValues)
    {
        if (CollectionUtils.isNotEmpty(refValues)) {
            this.refValues.addAll(refValues);
        }
        return this;
    }

    /**
     * Adds the refValue to the refValues list.
     *
     * @param refValue refValue to add
     * @return this object
     */
    public AbstractFilterBuilder<T> addReferenceValue(ReferenceValue refValue)
    {
        this.refValues.add(refValue);
        return this;
    }

    /**
     * Replaces the current list of refValues with the given one.
     *
     * @param refValue refValue to add
     * @return this object
     */
    public AbstractFilterBuilder<T> setReferenceValue(ReferenceValue refValue)
    {
        if (CollectionUtils.isNotEmpty(this.refValues)) {
            this.refValues = new LinkedList<>();
        }

        return this.addReferenceValue(refValue);
    }

    /**
     * Getter for refValues.
     *
     * @return refValues
     */
    public List<ReferenceValue> getRefValues()
    {
        return this.refValues;
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
     * Setter for docSpaceAndClass.
     *
     * @param docSpaceAndClass docSpaceAndClass to set
     * @return this object
     */
    public AbstractFilterBuilder<T> setDocSpaceAndClass(String docSpaceAndClass)
    {
        this.docSpaceAndClass = docSpaceAndClass;
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
    public AbstractFilterBuilder<T> setSpaceAndClass(String spaceAndClass)
    {
        this.spaceAndClass = spaceAndClass;
        return this;
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

    /**
     * Setter for propertyName.
     *
     * @param propertyName propertyName to set
     * @return this object
     */
    public AbstractFilterBuilder<T> setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;
        return this;
    }

    /**
     * Returns the parent DocumentSearchBuilder.
     * @return the parent DocumentSearchBuilder
     */
    public DocumentSearchBuilder back()
    {
        return this.parent;
    }

    /**
     * Getter for type.
     *
     * @return type
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * Setter for type.
     *
     * @param type type to set
     * @return this object
     */
    public AbstractFilterBuilder<T> setType(String type)
    {
        this.type = type;
        return this;
    }

    /**
     * Getter for minValue.
     *
     * @return minValue
     */
    public T getMinValue()
    {
        return this.minValue;
    }

    /**
     * Setter for minValue.
     *
     * @param minValue minValue to set
     * @return this object
     */
    public AbstractFilterBuilder<T> setMinValue(T minValue)
    {
        this.minValue = minValue;
        return this;
    }

    /**
     * Getter for maxValue.
     *
     * @return maxValue
     */
    public T getMaxValue()
    {
        return this.maxValue;
    }

    /**
     * Setter for maxValue.
     *
     * @param maxValue maxValue to set
     * @return this object
     */
    public AbstractFilterBuilder<T> setMaxValue(T maxValue)
    {
        this.maxValue = maxValue;
        return this;
    }

    /**
     * Getter for minKey.
     *
     * @return minKey
     */
    protected String getMinKey()
    {
        return this.minKey;
    }

    /**
     * Setter for minKey.
     *
     * @param minKey minKey to set
     * @return this object
     */
    protected AbstractFilterBuilder<T> setMinKey(String minKey)
    {
        this.minKey = minKey;
        return this;
    }

    /**
     * Getter for maxKey.
     *
     * @return maxKey
     */
    protected String getMaxKey()
    {
        return this.maxKey;
    }

    /**
     * Setter for maxKey.
     *
     * @param maxKey maxKey to set
     * @return this object
     */
    protected AbstractFilterBuilder<T> setMaxKey(String maxKey)
    {
        this.maxKey = maxKey;
        return this;
    }

    @SuppressWarnings("CyclomaticComplexity")
    @Override
    public JSONObject build()
    {
        JSONObject filter = new JSONObject();

        if (this.propertyName != null) {
            filter.put(PropertyName.PROPERTY_NAME_KEY, this.propertyName);
        }

        if (this.spaceAndClass != null) {
            filter.put(SpaceAndClass.CLASS_KEY, this.spaceAndClass);
        }

        if (this.docSpaceAndClass != null) {
            filter.put(AbstractFilter.DOC_CLASS_KEY, this.docSpaceAndClass);
        }

        if (CollectionUtils.isNotEmpty(this.values)) {
            JSONArray valuesArray = new JSONArray();

            for (Object value : this.values) {
                if (value != null) {
                    valuesArray.put(String.valueOf(value));
                }
            }

            filter.put(AbstractFilter.VALUES_KEY, valuesArray);
        }

        if (CollectionUtils.isNotEmpty(this.refValues)) {
            JSONArray refValuesArray = new JSONArray();

            for (ReferenceValue refValue : this.refValues) {
                if (refValue != null) {
                    refValuesArray.put(refValue.toJSONObject());
                }
            }

            filter.put(AbstractFilter.REF_VALUES_KEY, refValuesArray);
        }

        if (StringUtils.isNotBlank(this.type)) {
            filter.put(AbstractFilter.TYPE_KEY, this.type);
        }

        if (this.minValue != null) {
            filter.put(this.minKey, String.valueOf(this.minValue));
        }

        if (this.maxValue != null) {
            filter.put(this.maxKey, String.valueOf(this.maxValue));
        }

        return filter;
    }
}
