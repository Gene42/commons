/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils.web;

import com.gene42.commons.utils.exceptions.ServiceException;
import com.gene42.commons.utils.json.JsonApiBuilder;
import com.gene42.commons.utils.json.JsonApiErrorBuilder;

import java.util.EnumMap;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 * A utils class for Web related helper functions.
 *
 * @version $Id$
 */
@SuppressWarnings("checkstyle:cyclomaticcomplexity")
public final class WebUtils
{
    /** Key for offset in a json result object. */
    public static final String OFFSET_KEY = "offset";

    /** Key for limit in a json result object. */
    public static final String LIMIT_KEY = "limit";

    /** Key for total in a json result object. */
    public static final String TOTAL_KEY = "total";

    /** Key for returned in a json result object. */
    public static final String RETURNED_KEY = "returned";

    private static final EnumMap<ServiceException.Status, Response.Status> SERVICE_TO_WEB_MAP =
        new EnumMap<>(ServiceException.Status.class);

    static {
        SERVICE_TO_WEB_MAP.put(ServiceException.Status.DATA_ALREADY_EXISTS, Response.Status.CONFLICT);
        SERVICE_TO_WEB_MAP.put(ServiceException.Status.DATA_NOT_FOUND, Response.Status.NOT_FOUND);
        SERVICE_TO_WEB_MAP.put(ServiceException.Status.COULD_NOT_PARSE_DATA, Response.Status.BAD_REQUEST);
        SERVICE_TO_WEB_MAP.put(ServiceException.Status.INVALID_INPUT, Response.Status.BAD_REQUEST);
        SERVICE_TO_WEB_MAP.put(ServiceException.Status.SERVICE_UNAVAILABLE, Response.Status.SERVICE_UNAVAILABLE);
        SERVICE_TO_WEB_MAP.put(ServiceException.Status.UNAUTHORIZED, Response.Status.UNAUTHORIZED);
        SERVICE_TO_WEB_MAP.put(ServiceException.Status.FORBIDDEN, Response.Status.FORBIDDEN);
        SERVICE_TO_WEB_MAP.put(ServiceException.Status.INTERNAL_EXCEPTION, Response.Status.INTERNAL_SERVER_ERROR);
    }

    private WebUtils()
    {
        throw new AssertionError();
    }

    /**
     * Converts the given ServiceException into a WebApplicationException. If the ServiceException maps to
     * an Internal Server Error, the exception is logged using the given Logger.
     * @param e the ServiceException to convert
     * @param logger the logger to use for logging the exception
     * @throws WebApplicationException the equivalent ServiceException
     */
    public static void throwWebApplicationException(ServiceException e, Logger logger)
    {
        throw new WebApplicationException(e, getErrorResponse(e, logger));
    }


    /**
     * Returns a Response with a status value depending on the given ServiceException.Status value. The body of the
     * Response will contain the exception's message if available).
     * @param e the ServiceException to use for populating the Response object
     * @param logger the logger to use in case an error or fatal must be logged (can be null; will ignore logging)
     * @return a Response object
     */
    public static Response getErrorResponse(ServiceException e, Logger logger)
    {
        Response.ResponseBuilder builder;

        Integer code = e.getCode();
        Response.Status status = getReturnStatus(e);

        boolean internalError;
        if (code != null && code >= 400 && code < 600) {
            builder = Response.status(code);
            internalError = code >= 500;
        } else {
            builder = Response.status(status);
            internalError = Objects.equals(Response.Status.INTERNAL_SERVER_ERROR, status);
        }

        String message = e.getMessage();

        if (StringUtils.isBlank(message)) {
            message = "Uh oh, something went wrong on the server. Contact an admin if it persists.";
        }

        builder.entity(new JsonApiBuilder()
            .addError(new JsonApiErrorBuilder()
                .setStatus(String.valueOf(status.getStatusCode()))
                .setDetail(message)
                .setTitle(status.toString()))
                .build().toString());
        builder.type(MediaType.APPLICATION_JSON);

        if (internalError) {
            e.printStackTrace();
        } else if (logger != null) {
            logger.warn("Client error {}: {}", code == null ? status : code, message);
        }

        return builder.build();
    }

    private static Response.Status getReturnStatus(ServiceException e) {

        Response.Status status = null;

        if (e.getStatus() != null) {
            status = SERVICE_TO_WEB_MAP.get(e.getStatus());
        }

        if (status == null) {
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }

        return status;
    }

    /**
     * Returns a Response with a status value depending on the given status and the body containing the given message.
     * @param message the message to set as the Response body
     * @param status the status of the Response
     * @return a Response object
     */
    public static Response getErrorResponse(String message, Response.Status status)
    {
        return getErrorResponse(message, MediaType.TEXT_PLAIN_TYPE, status);
    }

    /**
     * Returns a Response with a status value depending on the given status and the body containing the given message.
     * @param message the message to set as the Response body
     * @param messageMediaType the media type of the message
     * @param status the status of the Response
     * @return a Response object
     */
    public static Response getErrorResponse(String message, MediaType messageMediaType, Response.Status status)
    {
        return Response.serverError().status(status).entity(message).type(messageMediaType).build();
    }

    /**
     * Returns a Response object based on the given parameters.
     * @param status the status to set
     * @param message the message to add to the response
     * @param uri the source uri to set in the response
     * @param param the source param to set in the response
     * @return a Response object
     */
    public static Response getErrorResponse(Response.Status status, String message, String uri, String param)
    {
        return Response.serverError()
            .status(status)
            .entity(new JsonApiBuilder()
                .addError(new JsonApiErrorBuilder()
                        .setStatus(String.valueOf(status.getStatusCode()))
                        .setDetail(message)
                        .setSourcePointer(uri)
                        .setSourceParameter(param)
                        .setTitle(status.toString()))
                .build().toString())
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    /**
     * Attempts to get the value found at the given key.
     * Throws a Bad Request exception if the given JSONObject does not have the given key,
     * or if the type does not match the provided Class.
     * @param object the JSONObject to check
     * @param key the key to check for
     * @param clazz the Class of the key's value
     * @param <T> the class of the instance to return
     * @return the value of the key
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T> T getJSONObjectValue(JSONObject object, String key, Class<T> clazz)
    {
        Object value = object.opt(key);

        if (value == null) {
            throw new WebApplicationException(getErrorResponse(
                Response.Status.BAD_REQUEST, "Key is missing", object.toString(), key));
        }

        return castJSONObject(value, clazz);
    }

    /**
     * Attempts to cast the given object to one of the possible Classes a JSONObject might return.
     * @param object the object to cast
     * @param clazz the Class of the object to cast to
     * @param <T> the class of the instance to return
     * @return the casted object
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T> T castJSONObject(@Nonnull Object object, Class<T> clazz)
    {
        if (jsonBadCast(object, clazz)) {
            throw new WebApplicationException(getErrorResponse(
                Response.Status.BAD_REQUEST, "Object is of wrong type", object.toString(), StringUtils
                    .substringAfterLast(clazz.toString(), ".")));
        }

        return (T) object;
    }

    /**
     * Attempts to parse the given string into a JSONObject and throws a WebApplicationException if it does not succeed:
     * invalid json, or null/empty input.
     * @param jsonString the input to parse
     * @return a JSONObject
     */
    @Nonnull
    public static JSONObject parseToJSONObject(String jsonString)
    {
        if (StringUtils.isEmpty(jsonString)) {
            throw new WebApplicationException(getErrorResponse("Empty JSON.", Response.Status.BAD_REQUEST));
        }

        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            throw new WebApplicationException(
                getErrorResponse(Response.Status.BAD_REQUEST, "Could not parse JSON.", e.getMessage(), jsonString)
            );
        }
    }

    /**
     * Returns a Response object based on the given parameters.
     * @param status the status to set
     * @param exception the exception to grab the message from
     * @param uri the source uri to set in the response
     * @param param the source param to set in the response
     * @return a Response object
     */
    public static Response getErrorResponse(Response.Status status, Throwable exception, String uri, String param)
    {
        return getErrorResponse(status, exception.getMessage(), uri, param);
    }

    /**
     * Does nothing.
     * @param e exception
     */
    public static void doNothing(Exception e)
    {
        // Do nothing
    }

    private static <T> boolean jsonBadCast(Object value, Class<T> clazz)
    {
        boolean stringBool = clazz == String.class && (value instanceof String);
        boolean jsonObjectBool = clazz == JSONObject.class && (value instanceof JSONObject);
        boolean jsonArrayBool = clazz == JSONArray.class && (value instanceof JSONArray);
        boolean boolBool = clazz == Boolean.class && (value instanceof Boolean);
        boolean one = stringBool || jsonObjectBool || jsonArrayBool || boolBool;

        boolean intBool = clazz == Integer.class && (value instanceof Integer);
        boolean doubleBool = clazz == Double.class && (value instanceof Double);
        boolean longBool = clazz == Long.class && (value instanceof Long);
        boolean numberBool = clazz == Number.class && (value instanceof Number);
        boolean two = intBool || doubleBool || longBool || numberBool;
        return !(one || two);
    }
}
