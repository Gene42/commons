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
