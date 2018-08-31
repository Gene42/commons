/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.controllers.converters;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.xpn.xwiki.objects.NumberProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.gene42.commons.utils.DateTools;
import com.gene42.commons.utils.exceptions.ServiceException;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.DBStringListProperty;
import com.xpn.xwiki.objects.ListProperty;
import com.xpn.xwiki.objects.PropertyInterface;
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
        tempMap.put(Integer.class, (from, to, fieldName)
                -> to.putOpt(fieldName, from.getIntValue(fieldName)));
        tempMap.put(Long.class, (from, to, fieldName)
                -> to.putOpt(fieldName, from.getLongValue(fieldName)));
        tempMap.put(Boolean.class, (from, to, fieldName)
            -> to.putOpt(fieldName, BooleanUtils.toBoolean(from.getIntValue(fieldName), 1, 0)));
        tempMap.put(Date.class, (from, to, fieldName)
            -> to.putOpt(fieldName, DateTools.dateToString(from.getDateValue(fieldName), DATE_TIME_FORMATTER)));
        tempMap.put(LocalDateTime.class, (from, to, fieldName)
                -> to.putOpt(fieldName, getStringFromLong(fieldName, from, StringUtils.EMPTY)));
        tempMap.put(List.class, (from, to, fieldName)
            -> to.putOpt(fieldName, toJSONArrayFromList(fieldName, from)));
        tempMap.put(Set.class, (from, to, fieldName)
            -> to.putOpt(fieldName, toJSONArrayFromSet(fieldName, from)));
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
        tempMap.put(Integer.class, (from, to, fieldName, context)
                -> to.setIntValue(fieldName, from.getInt(fieldName)));
        tempMap.put(Long.class, (from, to, fieldName, context)
                -> to.setLongValue(fieldName, from.getLong(fieldName)));
        tempMap.put(Boolean.class, (from, to, fieldName, context)
            -> to.setIntValue(fieldName, BooleanUtils.toInteger(from.getBoolean(fieldName), 1, 0)));
        tempMap.put(Date.class, (from, to, fieldName, context)
            -> to.setDateValue(fieldName, DateTools.stringToDate(from.getString(fieldName), DATE_TIME_FORMATTER)));
        tempMap.put(LocalDateTime.class, (from, to, fieldName, context)
                -> to.setLongValue(fieldName, DateTools.stringToMillis(from.getString(fieldName), null)));
        tempMap.put(List.class, (from, to, fieldName, context)
            -> putListInBaseObject(fromJSONArrayToList(fieldName, from), fieldName, to));
        tempMap.put(Set.class, (from, to, fieldName, context)
            -> putListInBaseObject(fromJSONArrayToListWithoutDuplicates(fieldName, from), fieldName, to));
        JSON_TO_X_OBJ = Collections.unmodifiableMap(tempMap);
    }

    /** Function map for comparing BaseObject. */
    protected static final Map<Class<?>, CompareXObj> COMPARE_X_OBJ;
    static {
        Map<Class<?>, CompareXObj> tempMap = new HashMap<>();
        tempMap.put(String.class, (o1, o2, fieldName, context)
            -> Objects.equals(o1.getStringValue(fieldName), o2.getStringValue(fieldName)));
        tempMap.put(TextAreaClass.class, (o1, o2, fieldName, context)
            -> Objects.equals(o1.getLargeStringValue(fieldName), o2.getLargeStringValue(fieldName)));
        tempMap.put(Double.class, (o1, o2, fieldName, context)
            -> Objects.equals(o1.getDoubleValue(fieldName), o2.getDoubleValue(fieldName)));
        tempMap.put(Integer.class, (o1, o2, fieldName, context)
                -> Objects.equals(o1.getIntValue(fieldName), o2.getIntValue(fieldName)));
        tempMap.put(Long.class, (o1, o2, fieldName, context)
                -> Objects.equals(o1.getLongValue(fieldName), o2.getLongValue(fieldName)));
        tempMap.put(Boolean.class, (o1, o2, fieldName, context)
            -> Objects.equals(BooleanUtils.toBoolean(o1.getIntValue(fieldName), 1, 0),
                BooleanUtils.toBoolean(o2.getIntValue(fieldName), 1, 0)));
        tempMap.put(Date.class, (o1, o2, fieldName, context)
            -> Objects.equals(DateTools.dateToString(o1.getDateValue(fieldName), DATE_TIME_FORMATTER),
                DateTools.dateToString(o2.getDateValue(fieldName), DATE_TIME_FORMATTER)));
        tempMap.put(LocalDateTime.class, (o1, o2, fieldName, context)
                -> Objects.equals(o1.getLongValue(fieldName), o2.getLongValue(fieldName)));
        tempMap.put(List.class, (o1, o2, fieldName, context)
            -> Objects.equals(getList(fieldName, o1), getList(fieldName, o2)));
        tempMap.put(Set.class, (o1, o2, fieldName, context)
            -> Objects.equals(getList(fieldName, o1), getList(fieldName, o2)));

        COMPARE_X_OBJ = Collections.unmodifiableMap(tempMap);
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
        return this.populateBaseObject(from, to, context, null, null);
    }

    /**
     * Populates the given BaseObject with values found in the given JSONObject. It uses the given keyTypesMapEntrySet
     * method to grab the needed mappings. While iterating over the entry set, if the incoming JSON object does not
     * contain the key, or the value of the key is null, the BaseObject property associated with that key should not
     * be updated. If keyTypesMapEntrySet is null, getKeyTypesMapEntrySet() is used to retrieve it.
     * If a function map is provided it will be used to convert between the two object types, otherwise the default one
     * will be used.
     * @param from the JSONObject used to populate the BaseObject
     * @param to the BaseObject to be populated with the properties found in the JSONObject
     * @param context XWikiContext used for populating the BaseObject
     * @return the same given BaseObject
     */
    protected BaseObject populateBaseObject(JSONObject from, BaseObject to, XWikiContext context,
        final Set<Map.Entry<String, Class<?>>> keyTypesMapEntrySet, final Map<Class<?>, JSONToXObj> functionMap)
    {
        if (from == null || to == null) {
            return to;
        }

        Set<Map.Entry<String, Class<?>>> _keyTypesMapEntrySet =
            (keyTypesMapEntrySet == null) ? this.getKeyTypesMapEntrySet() : keyTypesMapEntrySet;


        Map<Class<?>, JSONToXObj> _functionMap = (functionMap == null) ? this.getJSONToXObjFunctionMap() : functionMap;

        for (Map.Entry<String, Class<?>> entry : _keyTypesMapEntrySet) {
            JSONToXObj func = _functionMap.get(entry.getValue());
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
    public boolean equals(JSONObject jsonObject, BaseObject baseObject, XWikiContext context)
    {
        boolean result;
        if (jsonObject == null && baseObject == null) {
            result = true;
        } else if (jsonObject == null || baseObject == null) {
            result = false;
        } else {
            result = this.areBaseObjectsEqual(this.toBaseObject(jsonObject, context), baseObject, context);
        }

        return result;
    }

    private boolean areBaseObjectsEqual(BaseObject object1, BaseObject object2, XWikiContext context)
    {
        Map<Class<?>, CompareXObj> functionMap = this.getCompareXObjFunctionMap();

        for (Map.Entry<String, Class<?>> entry : this.getKeyTypesMapEntrySet()) {
            String key = entry.getKey();

            PropertyInterface value1 = object1.safeget(key);
            PropertyInterface value2 = object2.safeget(key);

            CompareXObj func = functionMap.get(entry.getValue());

            if ((value1 == null && value2 != null) || (value2 == null && value1 != null)
                    || (func != null && !func.equals(object1, object2, key, context))) {
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

    @Override
    public Map<Class<?>, CompareXObj> getCompareXObjFunctionMap() {
        return COMPARE_X_OBJ;
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

    private static JSONArray toJSONArrayFromList(String propertyName, BaseObject object)
    {
        JSONArray jsonArray = new JSONArray();

        try {
            PropertyInterface property = object.get(propertyName);

            if (!(property instanceof ListProperty)) {
                return jsonArray;
            }

            ListProperty listProperty = (ListProperty)property;

            for (String obj : listProperty.getList()) {
                jsonArray.put(obj);
            }

        } catch (XWikiException e) {
            return jsonArray;
        }

        return jsonArray;
    }

    private static JSONArray toJSONArrayFromSet(String propertyName, BaseObject object)
    {
        JSONArray jsonArray = new JSONArray();
        Set<String> current = new HashSet<>();

        try {
            PropertyInterface property = object.get(propertyName);

            if (!(property instanceof ListProperty)) {
                return jsonArray;
            }

            ListProperty listProperty = (ListProperty)property;

            for (String obj : listProperty.getList()) {
                if (!current.contains(obj)) {
                    current.add(obj);
                    jsonArray.put(obj);
                }
            }
        } catch (XWikiException e) {
            return jsonArray;
        }

        return jsonArray;
    }

    private static List<String> fromJSONArrayToList(String propertyName, JSONObject jsonObject)
    {
        JSONArray jsonArray = jsonObject.optJSONArray(propertyName);

        if (jsonArray == null) {
            return null;
        }

        List<String> resultList = new LinkedList<>();
        for (Object obj : jsonArray) {
            CollectionUtils.addIgnoreNull(resultList, getStringValue(obj));
        }

        return resultList;
    }

    private static List<String> fromJSONArrayToListWithoutDuplicates(String propertyName, JSONObject jsonObject)
    {
        JSONArray jsonArray = jsonObject.optJSONArray(propertyName);
        Set<String> current = new HashSet<>();

        if (jsonArray == null) {
            return null;
        }

        List<String> resultList = new LinkedList<>();
        for (Object obj : jsonArray) {
            if (obj == null) {
                continue;
            }
            String value = String.valueOf(obj);
            if (!current.contains(value)) {
                current.add(value);
                resultList.add(value);
            }
        }

        return resultList;
    }

    private static List<String> getList(String propertyName, BaseObject object)
    {
        if (object == null) {
            return null;
        }

        List<String> current = new LinkedList<>();

        try {
            PropertyInterface property = object.get(propertyName);

            if (!(property instanceof ListProperty)) {
                return current;
            }

            ListProperty listProperty = (ListProperty)property;

            return new LinkedList<>(listProperty.getList());

        } catch (XWikiException e) {
            return current;
        }
    }

    private static void putListInBaseObject(List<String> list, String fieldName, BaseObject to)
    {
        if (list == null) {
            return;
        }

        try {
            PropertyInterface propertyInterface = to.get(fieldName);
            DBStringListProperty listProperty;

            if (propertyInterface == null) {
                listProperty = new DBStringListProperty();
                to.put(fieldName, listProperty);
            } else if (propertyInterface instanceof DBStringListProperty) {
                listProperty = (DBStringListProperty) propertyInterface;
            } else {
                throw new ServiceException(String.format("Property [%s] is of unknown type [%s]",
                    fieldName, propertyInterface.getClass().getName()));
            }

            listProperty.setList(list);
            listProperty.setValueDirty(true);

        } catch (XWikiException | ServiceException e) {
            e.printStackTrace();
        }
    }

    private static String getStringValue(Object object) {
        if (object == null) {
            return null;
        } else {
            return String.valueOf(object);
        }
    }

    private static String getStringFromLong(String fieldName, BaseObject object, String defaultValue) {

        try {
            NumberProperty prop = (NumberProperty)object.safeget(fieldName);
            if (prop == null) {
                return defaultValue;
            }
            return  DateTools.millisToString(((Number)prop.getValue()).longValue(), null);
        } catch (Exception var3) {
            return defaultValue;
        }
    }
}
