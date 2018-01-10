package com.gene42.commons.controllers.locks;

import org.xwiki.component.annotation.Role;

import java.util.concurrent.TimeUnit;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
@Role
public interface LockWithTimeout
{
    /**
     * Returns the name of this lock.
     * @return the name
     */
    String getName();
    String getCurrentOwner(String entityId);

    /**
     * Acquire a lock for the given id. (Blocking). If the current thread already holds the lock, it will
     * return immediately.
     * @param entityId the id of the entity to acquire the lock for
     * @param timeoutValue number of time units the lock should last before it times out
     * @param timeoutUnit time unit of the timeout value
     * @param releasePermits number of times release needs to be called to actually release the lock
     */
    void acquireLock(String entityId, long timeoutValue, TimeUnit timeoutUnit, int releasePermits);

    /**
     * Acquire a lock for the given id. (Blocking). If the current thread already holds the lock, it will
     * return immediately.
     * @param entityId the id of the entity to acquire the lock for
     * @param timeoutValue number of time units the lock should last before it times out
     * @param timeoutUnit time unit of the timeout value
     */
    void acquireLock(String entityId, long timeoutValue, TimeUnit timeoutUnit);

    /**
     * Attempts to acquire a lock for the given id. (Non-Blocking). If it fails to get the lock it will return
     * immediately false. If the current thread already holds the lock, it will return true.
     * return immediately.
     * @param entityId the id of the entity to acquire the lock for
     * @param timeoutValue number of time units the lock should last before it times out
     * @param timeoutUnit time unit of the timeout value
     * @param releasePermits number of times release needs to be called to actually release the lock
     * @return true if it successfully acquired the lock, or if the current thread already holds the lock; it returns
     *         false if it did not successfully acquire the lock
     */
    boolean acquireLockOrReturn(String entityId, long timeoutValue, TimeUnit timeoutUnit, int releasePermits);

    /**
     * Release the lock for the given entity (Non-Blocking). If the lock has remaining remove permits,
     * the lock will not be removed.
     * @param entityId the id of the entity holding the lock
     */
    void releaseLock(String entityId);
}
