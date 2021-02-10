package com.gene42.commons.utils.exceptions;

/**
 * Exception thrown during Lock operations.
 * @version $Id$
 */
public class LockException extends ServiceException {

    /**
     * Constructor.
     */
    public LockException() {
        super();
    }

    /**
     * Constructor.
     * @param message the exception message
     */
    public LockException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param cause the exception cause
     */
    public LockException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     * @param message exception message
     * @param cause the exception cause
     */
    public LockException(String message, Throwable cause) {
        super(message, cause);
    }
}
