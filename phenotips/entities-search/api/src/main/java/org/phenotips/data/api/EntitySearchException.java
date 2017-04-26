/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api;

/**
 * Exception thrown during a DocumentSearch.
 *
 * @version $Id$
 */
public class EntitySearchException extends Exception
{
    /**
     * Constructor.
     */
    public EntitySearchException()
    {
        super();
    }

    /**
     * Constructor.
     * @param message message to associate with this exception
     */
    public EntitySearchException(String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * @param message  message to associate with this exception
     * @param throwable another Throwable to wrap with this exception
     */
    public EntitySearchException(String message, Throwable throwable)
    {
        super(message, throwable);
    }

    /**
     * Constructor.
     * @param throwable message to associate with this exception
     */
    public EntitySearchException(Throwable throwable)
    {
        super(throwable);
    }
}
