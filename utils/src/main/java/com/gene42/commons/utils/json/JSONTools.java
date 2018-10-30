/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils.json;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utils class for dealing with JSONObjects.
 *
 * @version $Id$
 */
public final class JSONTools
{
    private JSONTools()
    {
        // Do nothing.
    }

    /**
     * Retrieves a JSONArray from the given JSONObject with the specified key. If the key is not found an empty
     * JSONArray object is returned. If the key exists but is not a JSONArray, then a new JSONArray is created
     * the object at the specified key is added to the new array, and the new array is returned. Null key values
     * do not get added to the array.
     *
     * @param inputJSONObj the JSONObject to look into
     * @param key the key where the array should be found within the given JSONObject
     * @return a JSONArray object (is never null)
     */
    public static JSONArray getJSONArray(JSONObject inputJSONObj, String key)
    {
        Object valueObj = inputJSONObj.opt(key);

        JSONArray toReturn = new JSONArray();

        if (valueObj instanceof JSONArray) {
            toReturn = (JSONArray) valueObj;
        } else if (valueObj != null) {
            toReturn.put(valueObj);
        }

        return toReturn;
    }

    /**
     * Retrieves the values from the given JSONObject at the specified key, and returns them in a String list.
     * If the key does not exist an empty list is returned. If the key is a JSONArray any string value in the array
     * will be added to the result list, and any non string values will be passed through String.valueOf() before
     * being added. Null values are skipped. If the key value is a single value, the resulting list will be of size
     * one containing that value passed through String.valueOf().
     *
     * @param inputJSONObj the JSONObject to look into
     * @param key the key where the values should be found within the given JSONObject
     * @return  a String list (is never null)
     */
    public static List<String> getValues(JSONObject inputJSONObj, String key)
    {
        Object valueObj = inputJSONObj.opt(key);

        List<String> values = new LinkedList<>();

        if (valueObj == null) {
            return values;
        }

        if (valueObj instanceof JSONArray) {
            JSONArray valuesArray = (JSONArray) valueObj;
            for (Object objValue : valuesArray) {
                if (objValue instanceof String) {
                    values.add((String) objValue);
                } else if (objValue != null) {
                    values.add(String.valueOf(objValue));
                }
            }
        } else if (valueObj instanceof String) {
            values.add((String) valueObj);
        } else {
            values.add(String.valueOf(valueObj));
        }

        return values;
    }

    /**
     * Retrieves the value from the given JSONObject at the specified key as a String. If the key is missing or the
     * value is null, null is returned. If the key is a JSONArray the first entry in the array is returned as a string
     * using String.valueOf(). All other objects are returned after passing through String.valueOf().
     *
     * @param inputJSONObj the JSONObject to look into
     * @param key the key where the value should be found within the given JSONObject
     * @return a String or null if key does not exist or is null
     */
    public static String getValue(JSONObject inputJSONObj, String key)
    {

        if (inputJSONObj == null) {
            return null;
        }

        Object input = inputJSONObj.opt(key);

        String returnValue;

        if (input == null) {
            returnValue = null;
        } else if (input instanceof JSONArray) {
            JSONArray valuesArray = (JSONArray) input;
            if (valuesArray.length() == 0) {
                returnValue = null;
            } else {
                returnValue = String.valueOf(valuesArray.get(0));
            }
        } else if (input instanceof String) {
            returnValue = (String) input;
        } else {
            returnValue = String.valueOf(input);
        }
        return returnValue;
    }

    /**
     * Retrieves the value from the given JSONObject at the specified key as a String. If the value returned would be
     * null, the default value is returned instead.
     *
     * @param inputJSONObj the JSONObject to look into
     * @param key the key where the value should be found within the given JSONObject
     * @param defaultValue the value to use if key is not found or is null
     * @return a String or null if key does not exist or is null
     */
    public static String getValue(JSONObject inputJSONObj, String key, String defaultValue)
    {
        String value = getValue(inputJSONObj, key);

        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public static List<Object> jsonArrayToList(JSONArray array, boolean clone) {
        List<Object> results = new ArrayList<Object>(array.length());
        for (Object element : array) {
            if (element == null || JSONObject.NULL.equals(element)) {
                results.add(null);
            } else if (element instanceof JSONArray && clone) {
                results.add(jsonArrayToList(((JSONArray) element), true));
            } else if (element instanceof Map && clone) {
                results.add(new JSONObject((Map) element));
            } else {
                results.add(element);
            }
        }
        return results;
    }
}
