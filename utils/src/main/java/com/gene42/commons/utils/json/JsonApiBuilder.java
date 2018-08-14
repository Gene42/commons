/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils.json;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.Builder;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Builder class for a JSON API Response Object.
 *
 * @version $Id$
 */
public class JsonApiBuilder implements Builder<JSONObject>
{
    /** JSON API Field. */
    public static final String DATA_FIELD = "data";

    /** JSON API Field. */
    public static final String INCLUDED_FIELD = "included";

    /** JSON API Field. */
    public static final String ERRORS_FIELD = "errors";

    /** JSON API Field. */
    public static final String META_FIELD = "meta";


    private JSONObject meta = new JSONObject();
    private List<Builder<JSONObject>> data = new LinkedList<>();
    private List<Builder<JSONObject>> included = new LinkedList<>();
    private List<Builder<JSONObject>> errors = new LinkedList<>();

    /**
     * Adds the given Resource Builder to the data list.
     * @param data the resource builder to add
     * @return this object
     */
    public JsonApiBuilder addData(JsonApiResourceBuilder data)
    {
        if (data != null) {
            this.data.add(data);
        }
        return this;
    }

    /**
     * Adds the given Resource Builder to the included list.
     * @param included the resource builder to add
     * @return this object
     */
    public JsonApiBuilder addIncluded(JsonApiResourceBuilder included)
    {
        if (included != null) {
            this.included.add(included);
        }
        return this;
    }

    /**
     * Adds the given Error Builder to the errors list.
     * @param error the error builder to add
     * @return this object
     */
    public JsonApiBuilder addError(JsonApiErrorBuilder error)
    {
        if (error != null) {
            this.errors.add(error);
        }
        return this;
    }

    /**
     * Puts the given key/value to the meta object.
     * @param key the key of the attribute
     * @param value the value of the attribute
     * @return this object
     */
    public JsonApiBuilder putMeta(String key, Object value)
    {
        this.meta.put(key, value);
        return this;
    }

    /**
     * Puts all the key/value pairs of the given JSONObject to the meta object.
     * @param meta a JSONObject containing all meta attributes to copy over
     * @return this object
     */
    public JsonApiBuilder putMeta(JSONObject meta)
    {
        if (meta != null) {
            for (String key : meta.keySet()) {
                this.meta.put(key, meta.get(key));
            }
        }
        return this;
    }

    @Override
    public JSONObject build()
    {
        JSONObject result = new JSONObject();

        if (CollectionUtils.isEmpty(this.errors)) {
            if (this.data.size() == 1) {
                result.put(DATA_FIELD, this.data.get(0).build());
            } else {
                addCollection(DATA_FIELD, result, this.data);
            }
        }

        addCollection(INCLUDED_FIELD, result, this.included);
        addCollection(ERRORS_FIELD, result, this.errors);

        if (this.meta.length() > 0) {
            result.put(META_FIELD, this.meta);
        }

        return new JSONObject(result.toString());
    }

    @Override
    public String toString()
    {
        return this.build().toString();
    }

    private static void addCollection(String key, JSONObject result, Collection<Builder<JSONObject>> builders)
    {
        if (CollectionUtils.isNotEmpty(builders)) {
            JSONArray array = new JSONArray();
            for (Builder<JSONObject> builder : builders) {
                array.put(builder.build());
            }
            result.put(key, array);
        }
    }
}
