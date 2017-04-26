/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils.exceptions;

/**
 * General purpose exception to be used in the framework.
 *
 * @version $Id$
 */
public class ServiceException extends Exception
{
    private final Status status;

    /**
     * Constructor. The Status is set to Status.INTERNAL_EXCEPTION.
     */
    public ServiceException()
    {
        this(Status.INTERNAL_EXCEPTION);
    }

    /**
     * Constructor. Defaults to Status.INTERNAL_EXCEPTION.
     * @param e Throwable to wrap
     */
    public ServiceException(Throwable e)
    {
        this(Status.INTERNAL_EXCEPTION, e);
    }

    /**
     * Constructor.
     * @param status the Status to set
     */
    public ServiceException(Status status)
    {
        super();
        this.status = status;
    }

    /**
     * Constructor.
     * @param status the Status to set
     * @param message the message to set
     */
    public ServiceException(Status status, String message)
    {
        super(message);
        this.status = status;
    }

    /**
     * Constructor.
     * @param status the Status to set
     * @param message the message to set
     * @param e Throwable to wrap
     */
    public ServiceException(Status status, String message, Throwable e)
    {
        super(message, e);
        this.status = status;
    }

    /**
     * Constructor.
     * @param status the Status to set
     * @param e Throwable to wrap
     */
    public ServiceException(Status status, Throwable e)
    {
        super(e);
        this.status = status;
    }

    /**
     * Constructor.
     * @param message the message to set
     * @param e Throwable to wrap
     */
    public ServiceException(String message, Throwable e)
    {
        this(Status.INTERNAL_EXCEPTION, message, e);
    }

    /**
     * Getter for status.
     *
     * @return status
     */
    public Status getStatus()
    {
        return this.status;
    }

    /**
     * Status enum.
     */
    public enum Status
    {
        /** Value. */
        DATA_ALREADY_EXISTS,

        /** Value. */
        DATA_NOT_FOUND,

        /** Value. */
        COULD_NOT_SAVE_DATA,

        /** Value. */
        COULD_NOT_LOAD_DATA,

        /** Value. */
        INTERNAL_EXCEPTION,

        /** Value. */
        INVALID_INPUT
    }
}
