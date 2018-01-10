package com.gene42.commons.controllers.locks;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public class Lock
{
    private final String owner;
    private final AtomicLong timeout;
    private final AtomicInteger releasePermits;

    /**
     * Constructor.
     * @param owner the owner of the lock (some sort of id)
     * @param timeout millisecond representation of the datetime when the lock expires
     * @param releasePermits number of times release needs to be called to actually release the lock
     */
    public Lock(String owner, AtomicLong timeout, int releasePermits)
    {
        this.owner = owner;
        this.timeout = timeout;
        this.releasePermits = new AtomicInteger(releasePermits);
    }

    /**
     * Getter for owner.
     *
     * @return owner
     */
    public String getOwner()
    {
        return this.owner;
    }

    /**
     * Getter for timeout.
     *
     * @return timeout
     */
    public AtomicLong getTimeout()
    {
        return this.timeout;
    }

    /**
     * Getter for releasePermits.
     *
     * @return releasePermits
     */
    public AtomicInteger getReleasePermits()
    {
        return this.releasePermits;
    }
}
