package org.phenotips.data.api;

/**
 * Exception thrown during a DocumentSearch.
 *
 * @version $Id$
 */
public class DocumentSearchException extends Exception
{
    /**
     * Constructor.
     */
    public DocumentSearchException()
    {
        super();
    }

    /**
     * Constructor.
     * @param message message to associate with this exception
     */
    public DocumentSearchException(String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * @param message  message to associate with this exception
     * @param throwable another Throwable to wrap with this exception
     */
    public DocumentSearchException(String message, Throwable throwable)
    {
        super(message, throwable);
    }

    /**
     * Constructor.
     * @param throwable message to associate with this exception
     */
    public DocumentSearchException(Throwable throwable)
    {
        super(throwable);
    }
}
