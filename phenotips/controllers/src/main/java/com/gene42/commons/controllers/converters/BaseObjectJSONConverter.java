/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.controllers.converters;

import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.EntityReference;

import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Interface for BaseObject to JSON (and vice versa) Converters.
 *
 * @version $Id$
 */
@Role
public interface BaseObjectJSONConverter
{
    /**
     * Converts the given JSONObject into a XWiki BaseObject.
     * @param jsonObject the JSONObject to convert
     * @param context XWikiContext used for populating the BaseObject
     * @return a new BaseObject
     */
    BaseObject toBaseObject(JSONObject jsonObject, XWikiContext context);

    /**
     * Populates the given BaseObject with values found in the given JSONObject. It uses the getKeyTypesMapEntrySet()
     * method to grab the needed mappings. While iterating over the entry set, if the incoming JSON object does not
     * contain the key, or the value of the key is null, the BaseObject property associated with that key should not
     * be updated.
     * @param from the JSONObject used to populate the BaseObject
     * @param to the BaseObject to be populated with the properties found in the JSONObject
     * @param context XWikiContext used for populating the BaseObject
     * @return the same given BaseObject
     */
    BaseObject populateBaseObject(JSONObject from, BaseObject to, XWikiContext context);


    /**
     * Converts the given XWiki BaseObject into a JSONObject.
     * @param baseObject the BaseObject to convert
     * @return a JSONObject
     */
    JSONObject toJSONObject(BaseObject baseObject);

    /**
     * Populates the given JSONObject with values found in the given BaseObject. It uses the getKeyTypesMapEntrySet()
     * method to grab the needed mappings.
     * @param from the BaseObject used to populate the JSONObject
     * @param to the JSONObject to populate
     * @return the same given JSONObject
     */
    JSONObject populateJSONObject(BaseObject from, JSONObject to);

    /**
     * Compares the given JSONObject with the given BaseObject.
     * @param jsonObject a JSONObject
     * @param baseObject an XWiki BaseObject
     * @param context XWikiContext to use for conversion
     * @return true if both objects contain the same content, false otherwise
     */
    boolean equals(JSONObject jsonObject, BaseObject baseObject, XWikiContext context);

    /**
     * Returns an EntityReference to the XWiki class of the BaseObject.
     * @return a EntityReference
     */
    EntityReference getBaseObjectClassReference();

    /**
     * Returns an Map.Entry Set of the key/class map associated with the converter entry.
     *  {"id", String.class}, {"date", String.class}, {"isNormal", Boolean.class}, {"sd", Float.class} ...
     * @return a Set
     */
    Set<Map.Entry<String, Class<?>>> getKeyTypesMapEntrySet();

    /**
     * Returns the Function map for converting properties of a BaseObject into JSONObject entries.
     * @return a Map
     */
    Map<Class<?>, XObjToJSON> getXObjToJSONFunctionMap();

    /**
     * Returns the Function map for converting entries of a JSONObject into  BaseObject properties.
     * @return a Map
     */
    Map<Class<?>, JSONToXObj> getJSONToXObjFunctionMap();

    /**
     * Returns the Function map for converting entries of a JSONObject into  BaseObject properties.
     * @return a Map
     */
    Map<Class<?>, CompareXObj> getCompareXObjFunctionMap();

    /**
     * Functional Interface for converting a BaseObject to a JSONObject.
     */
    @FunctionalInterface
    interface XObjToJSON
    {
        /**
         * Takes the field from the BaseObject and inserts it into the given JSONObject.
         * @param from BaseObject to take from
         * @param to JSONObject to insert into
         * @param fieldName the field name to grab from the BaseObject
         */
        void apply(BaseObject from, JSONObject to, String fieldName);
    }

    /**
     * Functional Interface for converting a JSONObject to a BaseObject.
     */
    @FunctionalInterface
    interface JSONToXObj
    {
        /**
         * Takes the field from the JSONObject and inserts it into the given BaseObject.
         * @param from JSONObject to take from
         * @param to BaseObject to insert into
         * @param fieldName the field name to grab from the JSONObject
         */
        void apply(JSONObject from, BaseObject to, String fieldName, XWikiContext context);
    }

    /**
     * Functional Interface for converting a JSONObject to a BaseObject.
     */
    @FunctionalInterface
    interface CompareXObj
    {
        /**
         * Takes the field from the JSONObject and inserts it into the given BaseObject.
         * @param obj1 BaseObject first object to compare
         * @param obj2 BaseObject to insert into
         * @param fieldName the field name to grab from the JSONObject
         */
        boolean equals(BaseObject obj1, BaseObject obj2, String fieldName, XWikiContext context);
    }
}
