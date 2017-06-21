/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils.json;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.Builder;
import org.json.JSONObject;

/**
 * Builder class for a JSON API Resource Object.
 *
 * @version $Id$
 */
public class JsonApiResourceBuilder implements Builder<JSONObject>
{

    /** JSON API Field. */
    public static final String DATA_FIELD = "data";

    /** JSON API Field. */
    public static final String ID_FIELD = "id";

    /** JSON API Field. */
    public static final String TYPE_FIELD = "type";

    /** JSON API Field. */
    public static final String ATTRIBUTES_FIELD = "attributes";

    /** JSON API Field. */
    public static final String RELATIONSHIPS_FIELD = "relationships";

    /** JSON API Field. */
    public static final String PARENT_RELATIONSHIP_FIELD = "parent";

    /** JSON API Field. */
    public static final String LINKS_FIELD = "links";

    /** JSON API Field. */
    public static final String SELF_FIELD = "self";

    /** JSON API Field. */
    public static final String HREF_FIELD = "href";

    /** JSON API Field. */
    public static final String META_FIELD = "meta";

    private JSONObject attributes = new JSONObject();
    private JSONObject links = new JSONObject();
    private JSONObject relationships = new JSONObject();

    private String id;
    private String type;

    /**
     * Constructor.
     * @param id the id of the resource
     * @param type the type of resource
     */
    public JsonApiResourceBuilder(String id, String type)
    {
        this.id = id;
        this.type = type;
    }

    /**
     * Puts the given key/value as an attribute to this resource.
     * @param key the key of the attribute
     * @param value the value of the attribute
     * @return this object
     */
    public JsonApiResourceBuilder putAttribute(String key, Object value)
    {
        this.attributes.put(key, value);
        return this;
    }

    /**
     * Puts all the key/value pairs of the given JSONObject as attributes to this resource.
     * @param attributes a JSONObject containing all attributes to copy over
     * @return this object
     */
    public JsonApiResourceBuilder putAttributes(JSONObject attributes)
    {
        if (attributes != null) {
            for (String key : attributes.keySet()) {
                this.attributes.put(key, attributes.get(key));
            }
        }
        return this;
    }

    /**
     * Sets the self link for this resource.
     * @param link the string representation of the link
     * @return this object
     */
    public JsonApiResourceBuilder putSelfLink(String link)
    {
        this.links.put(SELF_FIELD, link);
        return this;
    }

    /**
     * Sets the link with the given name to the links object.
     * @param linkName the name of the link. If it equals 'self', the link will be handled as such.
     * @param href a string containing the link’s URL
     * @param meta a meta object containing non-standard meta-information about the link (can be null)
     * @return this object
     */
    public JsonApiResourceBuilder putLink(String linkName, String href, JSONObject meta)
    {
        if (StringUtils.equals(SELF_FIELD, linkName)) {
            return this.putSelfLink(href);
        } else {
            JSONObject linkObject = new JSONObject();
            linkObject.put(HREF_FIELD, href);
            linkObject.putOpt(META_FIELD, meta);
            this.links.put(linkName, linkObject);
            return this;
        }
    }

    /**
     * Sets the parent relationship for this resource.
     * @param id the id of the parent resource object
     * @param type the type of the parent resource object
     * @return this object
     */
    public JsonApiResourceBuilder putParentRelationship(String id, String type)
    {
        return this.putRelationship(PARENT_RELATIONSHIP_FIELD, id, type);
    }

    /**
     * Adds the given relationship, overriding the one there if it already exists.
     * @param relationshipName the name of the relationship (used as the key)
     * @param id the id of the resource object the relationship points to
     * @param type the type of the relationship resource object
     * @return this object
     */
    public JsonApiResourceBuilder putRelationship(String relationshipName, String id, String type)
    {
        JSONObject relationship = new JSONObject();
        JSONObject relationshipData = new JSONObject();
        relationship.put(DATA_FIELD, relationshipData);
        relationshipData.put(ID_FIELD, id);
        relationshipData.put(TYPE_FIELD, type);

        this.relationships.put(relationshipName, relationship);
        return this;
    }

    @Override
    public JSONObject build()
    {
        JSONObject resource = new JSONObject();
        resource.put(ID_FIELD, this.id);
        resource.put(TYPE_FIELD, this.type);

        if (this.attributes.length() > 0) {
            resource.put(ATTRIBUTES_FIELD, this.attributes);
        }

        if (this.links.length() > 0) {
            resource.put(LINKS_FIELD, this.links);
        }

        if (this.relationships.length() > 0) {
            resource.put(RELATIONSHIPS_FIELD, this.relationships);
        }

        return new JSONObject(resource.toString());
    }

    @Override
    public String toString()
    {
        return this.build().toString();
    }
}
