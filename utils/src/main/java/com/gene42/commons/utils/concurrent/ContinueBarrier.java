package com.gene42.commons.utils.concurrent;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Simple synchronization object between threads. Thread A can wait on this barrier while Thread B determines when
 * Thread A can continue. You should not mix and match wait and continue calls back and forth between threads.
 * If A calls continue it should always call continue. Use two of these objects for that a back and forth setup.
 *
 * You should not have more than one thread call wait, you should not have more than one thread call continue and
 * you should not call wait or continue twice or more in a row.
 *
 * @version $Id$
 */
public class ContinueBarrier {
    private final Semaphore barrier = new Semaphore(0);

    /**
     * Block until a continue order is given.
     *
     * @throws InterruptedException if the thread is interrupted
     */
    public void waitToContinue() throws InterruptedException {
        this.barrier.acquire();
    }

    /**
     * Block until a continue order is given or the time runs out.
     *
     * @param timeout the time value to wait for before giving up
     * @param timeUnit the unit of time for the timeout
     *
     * @return true if order to continue was given within the time frame, false otherwise
     * @throws InterruptedException if the thread is interrupted
     */
    public boolean waitToContinue(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return this.barrier.tryAcquire(timeout, timeUnit);
    }

    /**
     * Issue an order to continue to the waiting thread. Will only work if a wait was issue before.
     */
    public void canContinue() {
        if (this.barrier.availablePermits() == 0) {
            this.barrier.release();
        }
    }
}
