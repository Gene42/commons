/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils.web;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.gene42.commons.utils.exceptions.ServiceException;

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
     * @param logger the logger to use in case an error or fatal must be logged
     * @return a Response object
     */
    public static Response getErrorResponse(ServiceException e, Logger logger)
    {
        if (e.getStatus() == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        Response.Status status;
        String message = e.getMessage();

        switch (e.getStatus()) {
            case DATA_ALREADY_EXISTS:
                status = Response.Status.CONFLICT;
                break;
            case DATA_NOT_FOUND:
                status = Response.Status.NOT_FOUND;
                break;
            case INVALID_INPUT:
                status = Response.Status.BAD_REQUEST;
                break;
            case INTERNAL_EXCEPTION:
            case COULD_NOT_SAVE_DATA:
            case COULD_NOT_LOAD_DATA:
            default:
                status = Response.Status.INTERNAL_SERVER_ERROR;
                logger.error(status.toString(), e);
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
     * Does nothing.
     * @param e exception
     */
    public static void doNothing(Exception e)
    {
        // Do nothing
    }
}
