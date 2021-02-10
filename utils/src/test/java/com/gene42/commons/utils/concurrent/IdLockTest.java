package com.gene42.commons.utils.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.gene42.commons.utils.exceptions.LockException;
import com.gene42.commons.utils.exceptions.ServiceException;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IdLockTest {
    private static final boolean DEBUG = false;

    private static final long SLEEP_BASE = 2;
    private static final long SLEEP_EXTRA = 4;

    private static final String EXPIRING_THREAD = "Expiring thread";
    private static final String AFTER_EXPIRY_THREAD = "After expiry thread";
    private static final String SECOND_THREAD = "Second thread";

    private final List<String> ids = Arrays.asList("id1", "id2", "id3");


    @Test
    public void testLockWithSmallTimeout() throws Exception {
        this.testScenario(400, 5, TimeUnit.MILLISECONDS, SLEEP_BASE, SLEEP_EXTRA, this.ids, false);
    }

    @Test
    public void testLockWithLongTimeout() throws Exception {
        this.testScenario(100, 30, TimeUnit.MINUTES, SLEEP_BASE, SLEEP_EXTRA, this.ids, true);
    }

    @Test
    public void testAutoReleaseExpireBeforeTimeout() throws Exception {
        IdLock idLock = new IdLock(30, TimeUnit.MINUTES, 3, TimeUnit.SECONDS);
        Map<String, String> expectedResultMap = new HashMap<>();
        // The expiring thread will only lock once, and unlock will be unsuccessful
        expectedResultMap.put(EXPIRING_THREAD, "l");
        // Since the timeout is long, the after expiry the expired thread will unlock the invalid entry
        expectedResultMap.put(SECOND_THREAD, "lu");
        expectedResultMap.put(AFTER_EXPIRY_THREAD, "lu");
        this.autoReleaseScenario(idLock, expectedResultMap, "llulu");
    }

    @Test
    public void testAutoReleaseTimeoutBeforeExpiry() throws Exception {
        IdLock idLock = new IdLock(1, TimeUnit.SECONDS, 7, TimeUnit.SECONDS);
        Map<String, String> expectedResultMap = new HashMap<>();
        // The expiring thread will only lock once, and unlock will be unsuccessful
        expectedResultMap.put(EXPIRING_THREAD, "l");
        // Second thread will timeout so will never lock or unlock, just an error
        expectedResultMap.put(SECOND_THREAD, "e");
        // The thread after the expiry will be able to lock and unlock successfully
        expectedResultMap.put(AFTER_EXPIRY_THREAD, "lu");

        this.autoReleaseScenario(idLock, expectedResultMap, "lelu");
    }

    public void autoReleaseScenario(IdLock idLock, Map<String, String> expectedResultMap,
        String globalExpectedResult) {

        String id = "id1";

        StringBuffer resultBuffer = new StringBuffer();

        IdTaskRunnerWithBarrier expiringThread =
            new IdTaskRunnerWithBarrier(EXPIRING_THREAD, idLock, id, resultBuffer);
        IdTaskRunnerWithBarrier secondThread =
            new IdTaskRunnerWithBarrier(SECOND_THREAD, idLock, id, resultBuffer);
        IdTaskRunnerWithBarrier afterExpiryThread =
            new IdTaskRunnerWithBarrier(AFTER_EXPIRY_THREAD, idLock, id, resultBuffer);


        List<IdTaskRunnerWithBarrier> runnables = new ArrayList<>();
        runnables.add(expiringThread);
        runnables.add(secondThread);
        runnables.add(afterExpiryThread);

        ExecutorService executorService = Executors.newFixedThreadPool(runnables.size());

        try {
            runnables.forEach(executorService::submit);

            this.waitOnStartLoopBarrier(runnables);

            expiringThread.getLockBarrier().canContinue();
            expiringThread.getHasLockOrErrorBarrier().waitToContinue();

            secondThread.getLockBarrier().canContinue();

            this.waitForLockExpiry(idLock, id);

            afterExpiryThread.getLockBarrier().canContinue();
            afterExpiryThread.getHasLockOrErrorBarrier().waitToContinue();

            expiringThread.getUnlockBarrier().canContinue();

            afterExpiryThread.getUnlockBarrier().canContinue();

            secondThread.getHasLockOrErrorBarrier().waitToContinue();
            secondThread.getUnlockBarrier().canContinue();

            this.waitOnStartLoopBarrier(runnables);
            runnables.forEach(IdTaskRunnerWithBarrier::stop);

            String globalResult = resultBuffer.toString();
            if (DEBUG) {
                System.out.printf("Global result [%s] against expected [%s]\n", globalResult, globalExpectedResult);
            }

            runnables.forEach(r -> checkAutoReleaseResultEntry(r, expectedResultMap.get(r.getName())));
            assertEquals(globalExpectedResult, globalResult);
        } catch (Exception e) {
            fail();
        } finally {
            executorService.shutdown();
        }
    }

    private void waitOnStartLoopBarrier(List<IdTaskRunnerWithBarrier> runnables) throws InterruptedException {
        for (IdTaskRunnerWithBarrier runnable : runnables) {
            runnable.getLoopStartBarrier().waitToContinue();
        }
    }

    public void checkAutoReleaseResultEntry(IdTaskRunnerWithBarrier barrier, String expectedResult) {
        String result = barrier.getRunnerResult().toString();
        if (DEBUG) {
            System.out.printf("Checking %s - result [%s] against expected [%s]\n",
                barrier.getName(), result, expectedResult);
        }
        assertEquals(expectedResult, result);
    }

    private void waitForLockExpiry(IdLock idLock, String id) throws Exception {
        while (!Thread.currentThread().isInterrupted() && Thread.currentThread().isAlive()
            && !idLock.isLockExpired(id)
                     .orElseThrow(() -> new Exception("This should not happen"))) {
            Thread.sleep(500);
        }
    }

    public void testScenario(int runs, long timeout, TimeUnit timeUnit,
        long sleepBase,
        long sleepExtra, List<String> ids, boolean testErrorCount) throws Exception {

        IdLock idLock = new IdLock(timeout, timeUnit);
        ConcurrentMap<String, StringBuffer> resultMap = new ConcurrentHashMap<>();
        ConcurrentMap<String, Integer> errorCountMap = new ConcurrentHashMap<>();


        int threads = Runtime.getRuntime().availableProcessors();
        int runnables = threads * 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);

        try {
            Collection<Future<?>> futures = new ArrayList<>(runnables);

            int i = 0;
            for (; i < runnables; i++) {
                futures.add(executorService.submit(new IdLockRunner(
                    "runnable-" + (i + 1), idLock, runs, sleepBase, sleepExtra, ids, resultMap, errorCountMap)));
            }

            for (String id : ids) {
                futures.add(executorService.submit(
                    new IdLockUnlockOnlyRunner(
                        "runnable-" + (i + 1), id, idLock, runs, sleepBase, sleepExtra, ids, resultMap)));
                i++;
            }

            for (Future<?> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            fail();
        } finally {
            executorService.shutdown();
        }

        this.checkResults(idLock, resultMap);

        if (testErrorCount) {
            int errors = errorCountMap.values().stream().mapToInt(e -> e).sum();
            assertEquals(0, errors);
        }
    }

    private void checkResults(IdLock idLock, ConcurrentMap<String, StringBuffer> resultMap) throws Exception {
        for (Map.Entry<String, StringBuffer> entry : resultMap.entrySet()) {
            this.checkResultString(entry.getKey(), entry.getValue().toString());
        }

        assertEquals(0, idLock.getNumberOfLocks());
    }

    private void checkResultString(String id, String str) throws Exception {

        if (DEBUG) {
            System.out.printf("Checking %s - [%s]\n", id, str);
        }

        char lastChar = 'x';
        for (char c : str.toCharArray()) {

            if (lastChar == c) {
                throw new Exception("multiple locks or unlocks in a row:" + c);
            }

            lastChar = c;
        }
    }

    private static class IdLockRunner implements Callable<Boolean> {

        protected final String name;
        protected final IdLock idLock;

        protected final int runs;
        protected final long sleepBase;
        protected final long sleepExtra;
        protected final List<String> ids;
        private final ConcurrentMap<String, StringBuffer> resultMap;
        private final ConcurrentMap<String, Integer> errorCountMap;

        public IdLockRunner(
            String name,
            IdLock idLock,
            int runs,
            long sleepBase,
            long sleepExtra,
            List<String> ids,
            ConcurrentMap<String, StringBuffer> resultMap,
            ConcurrentMap<String, Integer> errorCountMap) {

            this.name = name;
            this.idLock = idLock;
            this.runs = runs;
            this.sleepBase = sleepBase;
            this.sleepExtra = sleepExtra;
            this.ids = ids;
            this.resultMap = resultMap;
            this.errorCountMap = errorCountMap;
        }

        @Override
        public Boolean call() throws Exception {
            int i = this.runs;
            while (i-- > 0) {
                this.lock(this.ids.get(Math.round((float) (Math.random() * (this.ids.size() - 1)))));
            }
            return true;
        }

        private void lock(String lockId) throws ServiceException {

            try (IdLock.Instance ignored = this.idLock.getLock(lockId).lock()) {

                if (DEBUG) {
                    System.out.printf("[%s] aquired lock [%s]%n", this.name, lockId);
                }

                StringBuffer buffer = this.resultMap.computeIfAbsent(lockId, (k) -> new StringBuffer()).append("l");

                this.sleep();

                buffer.append("u");
            } catch (LockException | InterruptedException e) {
                this.errorCountMap.compute(lockId, (k, v) -> v == null ? 1 : ++v);
                if (DEBUG) {
                    System.out.printf("[%s] could not lock [%s]%n", this.name, lockId);
                }
            }
        }

        protected void sleep() throws InterruptedException {
            Thread.sleep((long)(this.sleepBase + Math.random() * this.sleepExtra));
        }
    }

    private static class IdLockUnlockOnlyRunner extends IdLockRunner {
        private final String id;

        public IdLockUnlockOnlyRunner(String name,
            String id,
            IdLock idLock,
            int runs,
            long sleepBase,
            long sleepExtra,
            List<String> ids,
            ConcurrentMap<String, StringBuffer> resultMap) {

            super(name, idLock, runs, sleepBase, sleepExtra, ids, resultMap, null);
            this.id = id;
        }

        @Override
        public Boolean call() throws Exception {
            int i = 30;
            while (i-- > 0) {
                this.idLock.getLock(this.id).unlock();
                this.sleep();
            }
            return true;
        }
    }

    @RequiredArgsConstructor
    @Data
    private static class IdTaskRunnerWithBarrier implements Runnable {
        private final ContinueBarrier loopStartBarrier = new ContinueBarrier();
        private final ContinueBarrier lockBarrier = new ContinueBarrier();
        private final ContinueBarrier unlockBarrier = new ContinueBarrier();
        private final ContinueBarrier hasLockOrErrorBarrier = new ContinueBarrier();

        private final String name;
        private final IdLock idLock;
        private final String id;
        private final StringBuffer globalIdResult;
        private final StringBuilder runnerResult = new StringBuilder();

        private Thread myThread;

        public void stop() {
            if (this.myThread != null) {
                this.myThread.interrupt();
            }
        }

        @Override
        public void run() {
            this.myThread =  Thread.currentThread();

            while(!Thread.currentThread().isInterrupted() && Thread.currentThread().isAlive()) {
                try {
                    this.loopStartBarrier.canContinue();

                    IdLock.Instance lock = this.idLock.getLock(this.id);
                    if (DEBUG) {
                        System.out.printf("[%s] Waiting%n", this.name);
                    }
                    this.lockBarrier.waitToContinue();

                    lock.lock();
                    if (DEBUG) {
                        System.out.printf("[%s] aquired lock [%s]%n", this.name, this.id);
                    }
                    this.runnerResult.append("l");
                    this.globalIdResult.append("l");
                    this.hasLockOrErrorBarrier.canContinue();

                    this.unlockBarrier.waitToContinue();
                    boolean unlockAttempt = lock.unlock();
                    if (unlockAttempt) {
                        this.runnerResult.append("u");
                        this.globalIdResult.append("u");
                    }
                    if (DEBUG) {
                        System.out.printf("[%s] released lock on [%s]: %s%n", this.name, this.id, unlockAttempt);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (LockException e) {
                    this.runnerResult.append("e");
                    this.globalIdResult.append("e");
                    if (DEBUG) {
                        System.out.printf("[%s] could not lock [%s]%n", this.name, this.id);
                    }
                    this.hasLockOrErrorBarrier.canContinue();
                }
            }
        }
    }
}
