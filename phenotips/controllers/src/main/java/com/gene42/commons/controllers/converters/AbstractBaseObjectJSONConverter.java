/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.controllers.converters;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.gene42.commons.utils.DateTools;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.TextAreaClass;

/**
 * Abstract Converter for BaseObject to JSON (and vice versa). It holds most of the functionality. In most cases,
 * inheriting converters only need to implement getBaseObjectClassReference() and getKeyTypesMapEntrySet().
 *
 * @version $Id$
 */
public abstract class AbstractBaseObjectJSONConverter implements BaseObjectJSONConverter
{
    /** Date format. */
    protected static final String DATE_FORMAT = "yyyy-M-d";

    /** UTC Zone. */
    protected static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    /** Date Formatter. */
    protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(
        UTC_ZONE);

    /** Function map from BaseObject to JSON. */
    protected static final Map<Class<?>, XObjToJSON> X_OBJ_TO_JSON;
    static {
        Map<Class<?>, XObjToJSON> tempMap = new HashMap<>();
        tempMap.put(String.class, (from, to, fieldName)
            -> to.putOpt(fieldName, from.getStringValue(fieldName)));
        tempMap.put(TextAreaClass.class, (from, to, fieldName)
            -> to.putOpt(fieldName, from.getLargeStringValue(fieldName)));
        tempMap.put(Double.class, (from, to, fieldName)
            -> to.putOpt(fieldName, from.getDoubleValue(fieldName)));
        tempMap.put(Boolean.class, (from, to, fieldName)
            -> to.putOpt(fieldName, BooleanUtils.toBoolean(from.getIntValue(fieldName), 1, 0)));
        tempMap.put(Date.class, (from, to, fieldName)
            -> to.putOpt(fieldName, DateTools.dateToString(from.getDateValue(fieldName), DATE_TIME_FORMATTER)));

        X_OBJ_TO_JSON = Collections.unmodifiableMap(tempMap);
    }

    /** Function map from BaseObject to JSON. */
    protected static final Map<Class<?>, JSONToXObj> JSON_TO_X_OBJ;
    static {
        Map<Class<?>, JSONToXObj> tempMap = new HashMap<>();
        tempMap.put(String.class, (from, to, fieldName, context)
            -> to.setStringValue(fieldName, from.getString(fieldName)));
        tempMap.put(TextAreaClass.class, (from, to, fieldName, context)
            -> to.setLargeStringValue(fieldName, from.getString(fieldName)));
        tempMap.put(Double.class, (from, to, fieldName, context)
            -> to.setDoubleValue(fieldName, from.getDouble(fieldName)));
        tempMap.put(Boolean.class, (from, to, fieldName, context)
            -> to.setIntValue(fieldName, BooleanUtils.toInteger(from.getBoolean(fieldName), 1, 0)));
        tempMap.put(Date.class, (from, to, fieldName, context)
            -> to.setDateValue(fieldName, DateTools.stringToDate(from.getString(fieldName), DATE_TIME_FORMATTER)));

        JSON_TO_X_OBJ = Collections.unmodifiableMap(tempMap);
    }

    @Override
    public BaseObject toBaseObject(JSONObject jsonObject, XWikiContext context)
    {
        BaseObject xWikiObject = new BaseObject();
        xWikiObject.setXClassReference(this.getBaseObjectClassReference());
        return this.populateBaseObject(jsonObject, xWikiObject, context);
    }

    @Override
    public BaseObject populateBaseObject(JSONObject from, BaseObject to, XWikiContext context)
    {
        if (from == null || to == null) {
            return to;
        }
        Map<Class<?>, JSONToXObj> functionMap = this.getJSONToXObjFunctionMap();

        for (Map.Entry<String, Class<?>> entry : this.getKeyTypesMapEntrySet()) {
            JSONToXObj func = functionMap.get(entry.getValue());
            String key = entry.getKey();
            if (func != null && jsonValueIsNotNull(from, key, entry.getValue())) {
                func.apply(from, to, key, context);
            }
        }
        return to;
    }

    @Override
    public JSONObject toJSONObject(BaseObject baseObject)
    {
        return this.populateJSONObject(baseObject, new JSONObject());
    }

    @Override
    public JSONObject populateJSONObject(BaseObject from, JSONObject to)
    {
        if (from == null || to == null) {
            return to;
        }

        Map<Class<?>, XObjToJSON> functionMap = this.getXObjToJSONFunctionMap();

        for (Map.Entry<String, Class<?>> entry : this.getKeyTypesMapEntrySet()) {
            XObjToJSON func = functionMap.get(entry.getValue());
            if (func != null && from.safeget(entry.getKey()) != null) {
                func.apply(from, to, entry.getKey());
            }
        }
        return to;
    }

    @Override
    public boolean equals(JSONObject jsonObject, BaseObject baseObject)
    {
        boolean result;
        if (jsonObject == null && baseObject == null) {
            result = true;
        } else if (jsonObject == null || baseObject == null) {
            result = false;
        } else {
            result = areJSONObjectsEqual(jsonObject, this.toJSONObject(baseObject));
        }

        return result;
    }

    private static boolean areJSONObjectsEqual(JSONObject object1, JSONObject object2)
    {
        if (object1.length() != object2.length()) {
            return false;
        }

        for (String key : object1.keySet()) {
            if (!Objects.equals(object1.opt(key), object2.opt(key))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Map<Class<?>, XObjToJSON> getXObjToJSONFunctionMap()
    {
        return X_OBJ_TO_JSON;
    }

    @Override
    public Map<Class<?>, JSONToXObj> getJSONToXObjFunctionMap()
    {
        return JSON_TO_X_OBJ;
    }

    /**
     * Returns whether or not the given JSONObject has the given key and if it is not null. If the type is a number and
     * the value is an empty string, it is considered a null.
     * @param from the input JSONObject
     * @param key the key to check for
     * @param keyType the type the key is supposed to be
     * @return true if not null, false otherwise
     */
    public static boolean jsonValueIsNotNull(JSONObject from, String key, Class keyType)
    {
        if (!from.has(key) || from.isNull(key)) {
            return false;
        }

        Object value = from.get(key);

        if (value == null) {
            return false;
        }

        if (Number.class.isAssignableFrom(keyType)) {
            return !((value instanceof String) && StringUtils.isEmpty((String) value));
        }

        return true;
    }
}
