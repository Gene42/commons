/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils.web;

import java.util.EnumMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.gene42.commons.utils.exceptions.ServiceException;
import com.gene42.commons.utils.json.JsonApiBuilder;
import com.gene42.commons.utils.json.JsonApiErrorBuilder;

/**
 * A utils class for Web related helper functions.
 *
 * @version $Id$
 */
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
        SERVICE_TO_WEB_MAP.put(ServiceException.Status.INVALID_INPUT, Response.Status.BAD_REQUEST);
        SERVICE_TO_WEB_MAP.put(ServiceException.Status.SERVICE_UNAVAILABLE, Response.Status.SERVICE_UNAVAILABLE);
        SERVICE_TO_WEB_MAP.put(ServiceException.Status.UNAUTHORIZED, Response.Status.UNAUTHORIZED);
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
        throw new WebApplicationException(e, getErrorResponse(e, logger).getStatus());
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
        if (e.getStatus() == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        Response.Status status;
        String message = e.getMessage();

        status = SERVICE_TO_WEB_MAP.get(e.getStatus());

        if (status == null) {
            status = Response.Status.INTERNAL_SERVER_ERROR;
            if (logger != null) {
                logger.error(status.toString(), e);
            }
            message = "Uh oh, something went wrong on the server. Contact an admin if it persists.";
        }

        return Response.status(status).entity(message).build();
    }


    /**
     * Returns a Response with a status value depending on the given status and the body containing the given message.
     * @param message the message to set as the Response body
     * @param status the status of the Response
     * @return a Response object
     */
    public static Response getErrorResponse(String message, Response.Status status)
    {
        return Response.serverError().status(status).entity(message).type(MediaType.TEXT_PLAIN_TYPE).build();
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
     * Does nothing.
     * @param e exception
     */
    public static void doNothing(Exception e)
    {
        // Do nothing
    }
}
