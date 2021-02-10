package com.gene42.commons.utils.concurrent;

import java.io.Closeable;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jetbrains.annotations.NotNull;

import com.gene42.commons.utils.exceptions.LockException;

/**
 * <p>
 *     Provides functionality for locking against a string, usually an id of some kind.
 *     If timeout values are provided in the constructor, all calls to {@link IdLock.Instance#lock()} are called
 *     with these values. If the default constructor is used, the timeout will be the default one:
 *     {@value IdLock#DEFAULT_TIMEOUT} {@value IdLock#DEFAULT_TIMEOUT_UNIT}.
 * </p>
 * <p>
 *     This lock has an auto release feature. It is enabled by default and set to
 *     {@value IdLock#DEFAULT_AUTO_RELEASE_TIMEOUT} {@value IdLock#DEFAULT_AUTO_RELEASE_TIMEOUT_UNIT}.
 *     When a lock expires another thread will be able to acquire the lock even if another thread has it.
 *     This is done in order to prevent the scenario where the locking thread dies and does not release the lock,
 *     thus locking the id indefinitely.
 * </p>
 * <p>
 *     This lock auto cleans itself. When an unlock happens, if no other threads are waiting for a lock. The lock
 *     is completely removed. This means that as time passes, the structure holding the locks will not keep growing.
 * </p>
 * @version $Id$
 */
public class IdLock {

    /* Default lock timeout value. */
    public static final long DEFAULT_TIMEOUT = 30;

    /* Default lock timeout unit value. */
    public static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

    /* Default auto release timeout value. */
    public static final long DEFAULT_AUTO_RELEASE_TIMEOUT = 30;

    /* Default auto release timeout unit value. */
    public static final TimeUnit DEFAULT_AUTO_RELEASE_TIMEOUT_UNIT = TimeUnit.MINUTES;

    private static final String LOCK_ERROR_MESSAGE = "Could not establish lock for [%s]. Please try again.";

    private final ConcurrentHashMap<String, LockEntry> lockMap = new ConcurrentHashMap<>();

    private final long timeout;
    private final TimeUnit timeoutUnit;

    private final long autoReleaseTimeoutInMillis;

    /**
     * Constructor.
     */
    public IdLock() {
        this(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT);
    }

    /**
     * Constructor.
     *
     * @param timeout timeout value to use for all {@link IdLock.Instance#lock()} calls
     * @param timeoutUnit time unit value to use for all {@link IdLock.Instance#lock()} calls
     */
    public IdLock(long timeout, @NotNull TimeUnit timeoutUnit) {
        this(timeout, timeoutUnit, DEFAULT_AUTO_RELEASE_TIMEOUT, DEFAULT_AUTO_RELEASE_TIMEOUT_UNIT);
    }

    /**
     * Constructor.
     *
     * @param timeout timeout value to use for all {@link IdLock.Instance#lock()} calls
     * @param timeoutUnit time unit value to use for all {@link IdLock.Instance#lock()} calls
     */
    public IdLock(long timeout, @NotNull TimeUnit timeoutUnit, long autoReleaseTimeout,
        @NotNull TimeUnit autoReleaseTimeoutUnit) {
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.autoReleaseTimeoutInMillis = autoReleaseTimeoutUnit.toMillis(autoReleaseTimeout);
    }

    /**
     * Get a new instance of a CloseableIdLock wrapper around this IdLock.
     * @param id the id to lock
     * @return a new instance of a CloseableIdLock
     */
    public Instance getLock(@NotNull String id) {
        return new Instance(id);
    }

    /**
     * Return the number of id locks currently being used (one entry per id).
     * @return number of locks
     */
    public int getNumberOfLocks() {
        return this.lockMap.size();
    }

    /**
     * Returns whether or not the log for the given id is expired.
     *
     * @param id the id of the lock
     * @return an {@link Optional} containing the result. If empty it means that no lock for that id was found
     */
    public Optional<Boolean> isLockExpired(String id) {
        LockEntry entry = this.lockMap.getOrDefault(id, null);
        if (entry == null) {
            return Optional.empty();
        }
        long currentAcquireTime = entry.getAcquiredTime().get();
        boolean expired = System.currentTimeMillis() > currentAcquireTime + IdLock.this.autoReleaseTimeoutInMillis;
        return  Optional.of(expired);
    }

    private static class LockEntry {
        private final AtomicLong acquiredTime;
        private final ReentrantReadWriteLock mainLock = new ReentrantReadWriteLock();
        private final ReentrantReadWriteLock.WriteLock writeLock = mainLock.writeLock();
        private volatile boolean valid = true;

        public LockEntry(long acquiredTimeValue) {
             this.acquiredTime = new AtomicLong(acquiredTimeValue);
        }

        public AtomicLong getAcquiredTime() {
            return this.acquiredTime;
        }

        public ReentrantReadWriteLock.WriteLock getWriteLock() {
            return this.writeLock;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public ReentrantReadWriteLock getMainLock() {
            return mainLock;
        }
    }

    /**
     * {@link Closeable} instance of an {@link IdLock}.
     */
    public final class Instance implements Closeable {

        private final String id;
        private LockEntry acquiredLockEntry;

        /**
         * Constructor.
         * @param id the id to lock
         */
        public Instance(@NotNull String id) {
            this.id = id;
        }

        /**
         * Acquire lock on the id.
         *
         * @throws LockException if the lock is not successful
         */
        public Instance lock() throws LockException {
            this.lock(IdLock.this.timeout, IdLock.this.timeoutUnit);
            return this;
        }

        /**
         * Acquire lock on the id. Try for the given timeout amount then fail.
         *
         * @param timeout the time value to attempt acquiring the lock before giving up
         * @param timeoutUnit the time value unit to attempt acquiring the lock before giving up
         *
         * @throws LockException if the lock is not successful
         */
        public Instance lock(long timeout, @NotNull TimeUnit timeoutUnit) throws LockException {

            if (this.acquiredLockEntry != null) {
                throw new LockException("You cannot reuse a lock Instance");
            }

            long retryTime = timeoutUnit.toMillis(timeout);

            // There is one scenario where the entry might get deleted by an unlock between getting the item and
            // attempting to lock it. We will retry here a few times until we succeed or the time runs out. We
            // will use the same timeout as the lock acquiring one.
            while (retryTime > 0) {

                long start = System.currentTimeMillis();

                AtomicBoolean newLock = new AtomicBoolean(false);
                LockEntry lockEntry = IdLock.this.lockMap.compute(this.id, (k, v) -> createNewLockEntry(newLock, v));

                // If it's a new lock we already have the lock so one one would be able to delete it: make it invalid
                if (newLock.get()) {
                    this.acquiredLockEntry = lockEntry;
                    return this;
                }

                boolean cleanLock;
                try {
                    cleanLock = lockEntry.getWriteLock().tryLock(timeout, timeoutUnit);
                } catch (InterruptedException e) {
                    throw new LockException(String.format(LOCK_ERROR_MESSAGE, this.id));
                }

                if (lockEntry.isValid()) {
                    if (cleanLock) {
                        // Update acquired time
                        this.setAcquiredTime(lockEntry, lockEntry.getAcquiredTime().get());
                        this.acquiredLockEntry = lockEntry;
                        return this;
                    } else {
                        // If lockEntry is valid and we don't have a lock, we failed to get the lock
                        break;
                    }
                }

                // Lock entry is not valid, we need to retry
                retryTime -= System.currentTimeMillis() - start;
            }

            // If we reached this code, we don't have the lock
            throw new LockException(String.format(LOCK_ERROR_MESSAGE, this.id));
        }

        private void setAcquiredTime(LockEntry lock, long currentAcquireTime) throws LockException {
            boolean cleanAcquiredTimeSet =
                lock.getAcquiredTime().compareAndSet(currentAcquireTime, System.currentTimeMillis());

            if (!cleanAcquiredTimeSet) {
                throw new LockException(String.format(LOCK_ERROR_MESSAGE, this.id));
            }
        }

        /**
         * Release the lock for the id.
         * @return true only if the current thread successfully unlocked a valid entry
         *         cases where this might not be true are, if an exception was thrown during lock attempt, or if the
         *         lock was removed, is invalid or is a new lock held by a different thread
         */
        public boolean unlock() {
            AtomicBoolean unlockSuccessful = new AtomicBoolean(false);

            final LockEntry entryToUnlock = this.acquiredLockEntry;

            IdLock.this.lockMap.compute(this.id, (key, lockEntry) -> {

                if (lockEntry != entryToUnlock && entryToUnlock != null) {
                    entryToUnlock.setValid(false);
                    if (entryToUnlock.getWriteLock().isHeldByCurrentThread()) {
                        entryToUnlock.getWriteLock().unlock();
                    }
                    return lockEntry;
                }

                if (lockEntry == null) {
                    return null;
                }

                ReentrantReadWriteLock.WriteLock lock = lockEntry.getWriteLock();
                if (!lock.isHeldByCurrentThread()) {
                    return lockEntry;
                }

                unlockSuccessful.set(true);
                if (lockEntry.getMainLock().hasQueuedThreads()) {
                    lock.unlock();
                    return lockEntry;
                } else {
                    lockEntry.setValid(false);
                    lock.unlock();
                    return null;
                }
            });

            return unlockSuccessful.get();
        }

        @Override
        public void close() {
            this.unlock();
        }

        @NotNull
        private LockEntry createNewLockEntry(AtomicBoolean newLock, LockEntry currentLockEntry) {
            if (currentLockEntry != null && currentLockEntry.isValid()) {

                if (IdLock.DEFAULT_AUTO_RELEASE_TIMEOUT < 0) {
                    return currentLockEntry;
                }

                long currentAcquireTime = currentLockEntry.getAcquiredTime().get();

                // Check expiry
                boolean notExpired = System.currentTimeMillis() < currentAcquireTime
                    + IdLock.this.autoReleaseTimeoutInMillis;

                if (notExpired) {
                    return currentLockEntry;
                }

                currentLockEntry.setValid(false);
            }

            newLock.set(true);
            LockEntry lockEntry = new LockEntry(System.currentTimeMillis());
            // This should always succeed since we have just created the lock
            lockEntry.getWriteLock().tryLock();
            return lockEntry;
        }
    }


}
