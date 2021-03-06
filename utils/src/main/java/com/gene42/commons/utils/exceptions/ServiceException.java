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
    private static final Integer DEFAULT_CODE = null;
    private static final long serialVersionUID = -5902738439069545560L;

    private final Status status;
    private final Integer code;

    /**
     * Constructor. The Status is set to Status.INTERNAL_EXCEPTION.
     */
    public ServiceException()
    {
        this(Status.INTERNAL_EXCEPTION);
    }

    /**
     * Constructor. The Status is set to Status.INTERNAL_EXCEPTION.
     * @param message the message to set
     */
    public ServiceException(String message)
    {
        this(Status.INTERNAL_EXCEPTION, message);
    }

    /**
     * Constructor. Defaults to Status.INTERNAL_EXCEPTION.
     * @param e Throwable to wrap
     */
    public ServiceException(Throwable e)
    {
        this(transferStatus(e, Status.INTERNAL_EXCEPTION), e.getMessage(), e);
    }

    /**
     * Constructor.
     * @param status the Status to set
     */
    public ServiceException(Status status)
    {
        super();
        this.status = status;
        this.code = DEFAULT_CODE;
    }

    /**
     * Constructor.
     * @param code the Status to set
     */
    public ServiceException(int code)
    {
        super();
        this.code = code;
        this.status = Status.INTERNAL_EXCEPTION;
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
        this.code = DEFAULT_CODE;
    }

    /**
     * Constructor.
     * @param code the Status to set
     * @param message the message to set
     */
    public ServiceException(int code, String message)
    {
        super(message);
        this.code = code;
        this.status = Status.INTERNAL_EXCEPTION;
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
        this.code = DEFAULT_CODE;
    }

    /**
     * Constructor.
     * @param code the Status to set
     * @param message the message to set
     * @param e Throwable to wrap
     */
    public ServiceException(int code, String message, Throwable e)
    {
        super(message, e);
        this.code = code;
        this.status = Status.INTERNAL_EXCEPTION;
    }

    /**
     * Constructor.
     * @param status the Status to set
     * @param e Throwable to wrap
     */
    public ServiceException(Status status, Throwable e)
    {
        super(e.getMessage(), e);
        this.status = status;
        this.code = DEFAULT_CODE;
    }

    /**
     * Constructor.
     * @param code the Status to set
     * @param e Throwable to wrap
     */
    public ServiceException(int code, Throwable e)
    {
        super(e.getMessage(), e);
        this.code = code;
        this.status = Status.INTERNAL_EXCEPTION;
    }

    /**
     * Constructor.
     * @param message the message to set
     * @param e Throwable to wrap
     */
    public ServiceException(String message, Throwable e)
    {
        this(transferStatus(e, Status.INTERNAL_EXCEPTION), message, e);

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
     * Getter for code.
     *
     * @return code
     */
    public Integer getCode()
    {
        return this.code;
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
        COULD_NOT_PARSE_DATA,

        /** Value. */
        INTERNAL_EXCEPTION,

        /** Value. */
        INVALID_INPUT,

        /** Value. */
        SERVICE_UNAVAILABLE,

        /** Value. */
        UNAUTHORIZED,

        /** Value. */
        FORBIDDEN
    }

    private static Status transferStatus(Throwable e, Status defaultStatus)
    {
        if (e instanceof ServiceException) {
            return ((ServiceException) e).getStatus();
        } else {
            return defaultStatus;
        }
    }
}
