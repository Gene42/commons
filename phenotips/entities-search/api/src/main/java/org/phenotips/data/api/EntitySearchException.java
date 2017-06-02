/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api;

import com.gene42.commons.utils.exceptions.ServiceException;

/**
 * Exception thrown during a DocumentSearch.
 *
 * @version $Id$
 */
public class EntitySearchException extends ServiceException
{
    /**
     * Constructor. The Status is set to Status.INTERNAL_EXCEPTION.
     */
    public EntitySearchException()
    {
        super();
    }

    /**
     * Constructor. The Status is set to Status.INTERNAL_EXCEPTION.
     * @param message the message to set
     */
    public EntitySearchException(String message)
    {
        super(message);
    }

    /**
     * Constructor. Defaults to Status.INTERNAL_EXCEPTION.
     * @param e Throwable to wrap
     */
    public EntitySearchException(Throwable e)
    {
        super(e);
    }

    /**
     * Constructor.
     * @param status the Status to set
     */
    public EntitySearchException(Status status)
    {
        super(status);
    }

    /**
     * Constructor.
     * @param status the Status to set
     * @param message the message to set
     */
    public EntitySearchException(Status status, String message)
    {
        super(status, message);
    }

    /**
     * Constructor.
     * @param status the Status to set
     * @param message the message to set
     * @param e Throwable to wrap
     */
    public EntitySearchException(Status status, String message, Throwable e)
    {
        super(status, message, e);
    }

    /**
     * Constructor.
     * @param status the Status to set
     * @param e Throwable to wrap
     */
    public EntitySearchException(Status status, Throwable e)
    {
        super(status, e);
    }

    /**
     * Constructor.
     * @param message the message to set
     * @param e Throwable to wrap
     */
    public EntitySearchException(String message, Throwable e)
    {
        super(message, e);
    }

}
