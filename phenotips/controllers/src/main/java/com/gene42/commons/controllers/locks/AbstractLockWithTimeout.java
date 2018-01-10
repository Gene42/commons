package com.gene42.commons.controllers.locks;

import org.xwiki.text.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public abstract class AbstractLockWithTimeout implements LockWithTimeout
{
    /** Lock map. */
    private final ConcurrentMap<String, Lock> lockMap = new ConcurrentHashMap<>();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void acquireLock(String entityId, long timeoutValue, TimeUnit timeoutUnit, int releasePermits)
    {
        this.acquireLock(entityId, timeoutValue, timeoutUnit, releasePermits, false);
    }

    @Override
    public boolean acquireLockOrReturn(String entityId, long timeoutValue, TimeUnit timeoutUnit, int releasePermits)
    {
        return this.acquireLock(entityId, timeoutValue, timeoutUnit, releasePermits, true) != null;
    }

    @Override
    public void acquireLock(String entityId, long timeoutValue, TimeUnit timeoutUnit)
    {
        this.acquireLock(entityId, timeoutValue, timeoutUnit, 1);
    }

    @Override
    public void releaseLock(String entityId)
    {
        Lock current = this.lockMap.get(entityId);
        if (current == null) {
            return;
        }

        if (hasExpired(System.currentTimeMillis(), current)
            || (currentThreadHasLock(current) && current.getReleasePermits().decrementAndGet() <= 0)) {
           this.clearLock(entityId, current);
        } else {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug(String.format("Thread [%s] attempted to remove lock for %s [%s] (%s)",
                    Thread.currentThread().getId(), this.getName(), entityId, current.getReleasePermits().get()));
            }
        }
    }


    @Override
    public String getCurrentOwner(String entityId)
    {
        Lock lock = this.lockMap.get(entityId);
        if (lock == null) {
            return null;
        }
        return lock.getOwner();
    }

    private Lock acquireLock(String entityId, long timeoutValue, TimeUnit timeoutUnit, int releasePermits,
        boolean returnIfFailed)
    {
        while (true) {

            long currentTime = System.currentTimeMillis();
            long myLockTime = currentTime + timeoutUnit.toMillis(timeoutValue);

            Lock newLock = new Lock(String.valueOf(Thread.currentThread().getId()),
                new AtomicLong(myLockTime), releasePermits);
            Lock previous = this.lockMap.putIfAbsent(entityId, newLock);

            if (previous == null) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(String.format("Thread [%s] acquired lock for %s [%s] (%s)",
                        Thread.currentThread().getId(), this.getName(), entityId, releasePermits));
                }
                // I acquired the lock
                return newLock;
            }

            if (hasExpired(currentTime, previous)) {
                releaseLock(entityId);
            } else if (currentThreadHasLock(previous)) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(String.format("Thread [%s] already has the lock for %s [%s] (%s)",
                        Thread.currentThread().getId(), this.getName(), entityId, releasePermits));
                }
                return previous;
            } else {

                if (returnIfFailed) {
                    return null;
                }
                // Lock is still valid
                // Need to wait
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(String.format("Lock is valid. Thread [%s] will wait for unlock for %s [%s]",
                        Thread.currentThread().getId(), this.getName(), entityId));
                }
                synchronized (this.lockMap) {
                    try {
                        this.lockMap.wait(4_000L);
                    } catch (InterruptedException e) {
                        return null;
                    }
                }
            }
        }
    }

    private void clearLock(String entityId, Lock lock) {
        if (lock.getTimeout().compareAndSet(lock.getTimeout().get(), System.currentTimeMillis() + 60_000L)) {
            this.lockMap.remove(entityId);

            synchronized (this.lockMap) {
                this.lockMap.notifyAll();
            }
            if (this.logger.isDebugEnabled()) {
                this.logger.debug(String.format("Thread [%s] removed lock for %s [%s]",
                    Thread.currentThread().getId(), this.getName(), entityId));
            }
        }
    }

    private static boolean currentThreadHasLock(Lock lock)
    {
        return lock != null && StringUtils.equals(lock.getOwner(), String.valueOf(Thread.currentThread().getId()));
    }

    private static boolean hasExpired(long currentTime, Lock lock) {
        return currentTime > lock.getTimeout().get();
    }

}
