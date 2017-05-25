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
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
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
    protected static final Map<Class, XObjToJSON> X_OBJ_TO_JSON;
    static {
        Map<Class, XObjToJSON> tempMap = new HashMap<>();
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
    protected static final Map<Class, JSONToXObj> JSON_TO_X_OBJ;
    static {
        Map<Class, JSONToXObj> tempMap = new HashMap<>();
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
        return this.populateBaseObject(from, to, context, this.getKeyTypesMapEntrySet());
    }

    @Override
    public BaseObject populateBaseObject(JSONObject from, BaseObject to, XWikiContext context,
        Set<Map.Entry<String, Class>> keyTypesMapEntrySet)
    {
        return this.populateBaseObject(from, to, context, keyTypesMapEntrySet, this.getJSONToXObjFunctionMap());
    }

    @Override
    public BaseObject populateBaseObject(JSONObject from, BaseObject to, XWikiContext context,
        Set<Map.Entry<String, Class>> keyTypesMapEntrySet, Map<Class, JSONToXObj> functionMap)
    {
        for (Map.Entry<String, Class> entry : keyTypesMapEntrySet) {
            JSONToXObj func = functionMap.get(entry.getValue());
            if (func != null && from.has(entry.getKey())) {
                func.apply(from, to, entry.getKey(), context);
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
        return this.populateJSONObject(from, to, this.getKeyTypesMapEntrySet());
    }

    @Override
    public JSONObject populateJSONObject(BaseObject from, JSONObject to,
        Set<Map.Entry<String, Class>> keyTypesMapEntrySet)
    {
        return this.populateJSONObject(from, to, keyTypesMapEntrySet, this.getXObjToJSONFunctionMap());
    }

    @Override
    public JSONObject populateJSONObject(BaseObject from, JSONObject to,
        Set<Map.Entry<String, Class>> keyTypesMapEntrySet, Map<Class, XObjToJSON> functionMap)
    {
        for (Map.Entry<String, Class> entry : keyTypesMapEntrySet) {
            XObjToJSON func = functionMap.get(entry.getValue());
            if (func != null) {
                func.apply(from, to, entry.getKey());
            }
        }
        return to;
    }

    @Override
    public Map<Class, XObjToJSON> getXObjToJSONFunctionMap()
    {
        return X_OBJ_TO_JSON;
    }

    @Override
    public Map<Class, JSONToXObj> getJSONToXObjFunctionMap()
    {
        return JSON_TO_X_OBJ;
    }
}
