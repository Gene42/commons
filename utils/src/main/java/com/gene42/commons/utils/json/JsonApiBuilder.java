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
    public static final String INCLUDED_FIELD = "included";

    /** JSON API Field. */
    public static final String META_FIELD = "meta";


    private JSONObject meta = new JSONObject();
    private List<JsonApiResourceBuilder> data = new LinkedList<>();
    private List<JsonApiResourceBuilder> included = new LinkedList<>();

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

        addCollection(JsonApiResourceBuilder.DATA_FIELD, result, this.data);
        addCollection(INCLUDED_FIELD, result, this.included);

        if (this.meta.length() > 0) {
            result.put(META_FIELD, this.meta);
        }

        return result;
    }

    @Override
    public String toString()
    {
        return this.build().toString();
    }

    private static void addCollection(String key, JSONObject result, Collection<JsonApiResourceBuilder> builders)
    {
        if (CollectionUtils.isNotEmpty(builders)) {
            JSONArray array = new JSONArray();
            for (JsonApiResourceBuilder builder : builders) {
                array.put(builder.build());
            }
            result.put(key, array);
        }
    }
}
