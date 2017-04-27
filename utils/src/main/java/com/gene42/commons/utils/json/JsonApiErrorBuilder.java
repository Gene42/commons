package com.gene42.commons.utils.json;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.Builder;
import org.json.JSONObject;

/**
 * Builder class for a JSON API Error Object.
 *
 * @version $Id$
 */
public class JsonApiErrorBuilder implements Builder<JSONObject>
{
    /** JSON API Field. */
    public static final String ID_FIELD = "id";

    /** JSON API Field. */
    public static final String STATUS_FIELD = "status";

    /** JSON API Field. */
    public static final String CODE_FIELD = "code";

    /** JSON API Field. */
    public static final String TITLE_FIELD = "title";

    /** JSON API Field. */
    public static final String DETAIL_FIELD = "detail";

    /** JSON API Field. */
    public static final String SOURCE_FIELD = "source";

    /** JSON API Field. */
    public static final String LINKS_FIELD = "links";

    /** JSON API Field. */
    public static final String ABOUT_FIELD = "about";

    /** JSON API Field. */
    public static final String POINTER_FIELD = "pointer";

    /** JSON API Field. */
    public static final String PARAMETER_FIELD = "parameter";

    /** JSON API Field. */
    public static final String META_FIELD = "meta";

    private String id;
    private String status;
    private String code;
    private String title;
    private String detail;
    private String aboutLink;
    private String sourcePointer;
    private String sourceParameter;
    private JSONObject meta = new JSONObject();

    /**
     * Getter for id.
     *
     * @return id
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * Setter for id.
     *
     * @param id id to set
     * @return this object
     */
    public JsonApiErrorBuilder setId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Getter for status.
     *
     * @return status
     */
    public String getStatus()
    {
        return this.status;
    }

    /**
     * Setter for status.
     *
     * @param status status to set
     * @return this object
     */
    public JsonApiErrorBuilder setStatus(String status)
    {
        this.status = status;
        return this;
    }

    /**
     * Getter for code.
     *
     * @return code
     */
    public String getCode()
    {
        return this.code;
    }

    /**
     * Setter for code.
     *
     * @param code code to set
     * @return this object
     */
    public JsonApiErrorBuilder setCode(String code)
    {
        this.code = code;
        return this;
    }

    /**
     * Getter for title.
     *
     * @return title
     */
    public String getTitle()
    {
        return this.title;
    }

    /**
     * Setter for title.
     *
     * @param title title to set
     * @return this object
     */
    public JsonApiErrorBuilder setTitle(String title)
    {
        this.title = title;
        return this;
    }

    /**
     * Getter for detail.
     *
     * @return detail
     */
    public String getDetail()
    {
        return this.detail;
    }

    /**
     * Setter for detail.
     *
     * @param detail detail to set
     * @return this object
     */
    public JsonApiErrorBuilder setDetail(String detail)
    {
        this.detail = detail;
        return this;
    }

    /**
     * Getter for aboutLink.
     *
     * @return aboutLink
     */
    public String getAboutLink()
    {
        return this.aboutLink;
    }

    /**
     * Setter for aboutLink.
     *
     * @param aboutLink aboutLink to set
     * @return this object
     */
    public JsonApiErrorBuilder setAboutLink(String aboutLink)
    {
        this.aboutLink = aboutLink;
        return this;
    }

    /**
     * Getter for sourcePointer.
     *
     * @return sourcePointer
     */
    public String getSourcePointer()
    {
        return this.sourcePointer;
    }

    /**
     * Setter for sourcePointer.
     *
     * @param sourcePointer sourcePointer to set
     * @return this object
     */
    public JsonApiErrorBuilder setSourcePointer(String sourcePointer)
    {
        this.sourcePointer = sourcePointer;
        return this;
    }

    /**
     * Getter for sourceParameter.
     *
     * @return sourceParameter
     */
    public String getSourceParameter()
    {
        return this.sourceParameter;
    }

    /**
     * Setter for sourceParameter.
     *
     * @param sourceParameter sourceParameter to set
     * @return this object
     */
    public JsonApiErrorBuilder setSourceParameter(String sourceParameter)
    {
        this.sourceParameter = sourceParameter;
        return this;
    }

    /**
     * Getter for meta.
     *
     * @return meta
     */
    public JSONObject getMeta()
    {
        return new JSONObject(this.meta.toString());
    }



    /**
     * Puts the given key/value to the meta object.
     * @param key the key of the attribute
     * @param value the value of the attribute
     * @return this object
     */
    public JsonApiErrorBuilder putMeta(String key, Object value)
    {
        this.meta.put(key, value);
        return this;
    }

    @Override
    public JSONObject build()
    {
        JSONObject result = new JSONObject();

        if (StringUtils.isNotBlank(this.id)) {
            result.put(ID_FIELD, this.id);
        }

        if (StringUtils.isNotBlank(this.status)) {
            result.put(STATUS_FIELD, this.status);
        }

        if (StringUtils.isNotBlank(this.code)) {
            result.put(CODE_FIELD, this.code);
        }

        if (StringUtils.isNotBlank(this.title)) {
            result.put(TITLE_FIELD, this.title);
        }

        if (StringUtils.isNotBlank(this.detail)) {
            result.put(DETAIL_FIELD, this.detail);
        }

        if (StringUtils.isNotBlank(this.aboutLink)) {
            JSONObject links = new JSONObject();
            result.put(LINKS_FIELD, links);
            links.put(ABOUT_FIELD, this.aboutLink);
        }

        this.handleSource(result);

        if (this.meta.length() > 0) {
            result.put(META_FIELD, this.getMeta());
        }

        return result;
    }

    private void handleSource(JSONObject result)
    {
        if (StringUtils.isNotBlank(this.sourcePointer) || StringUtils.isNotBlank(this.sourceParameter)) {
            JSONObject source = new JSONObject();
            result.put(SOURCE_FIELD, source);

            if (StringUtils.isNotBlank(this.sourcePointer)) {
                source.put(POINTER_FIELD, this.sourcePointer);
            }

            if (StringUtils.isNotBlank(this.sourceParameter)) {
                source.put(PARAMETER_FIELD, this.sourceParameter);
            }
        }
    }
}
