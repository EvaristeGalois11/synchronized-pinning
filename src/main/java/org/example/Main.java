package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Use synchronized");
        startWaiting(true);
        logger.info("Use reentrant lock");
        startWaiting(false);
    }

    private static void startWaiting(boolean useSynchronized) {
        try (var executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                futures.add(executorService.submit(new Waiting(useSynchronized)));
            }
            if (futures.stream().allMatch(Main::eval)) {
                logger.info("Every virtual thread seem to be pinned to its carrier thread");
            } else {
                logger.info("There is at least one virtual thread that unmounted from its carrier thread");
            }
        }
    }

    private static boolean eval(Future<Boolean> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class Waiting implements Callable<Boolean> {
        private static final Object synchronizedLock = new Object();
        private static final ReentrantLock reentrantLock = new ReentrantLock();

        private final boolean useSynchronized;

        public Waiting(boolean useSynchronized) {
            this.useSynchronized = useSynchronized;
        }

        @Override
        public Boolean call() {
            return useSynchronized ? waitSynchronized() : waitReentrantLock();
        }

        private boolean waitSynchronized() {
            var before = Thread.currentThread().toString();
            synchronized (synchronizedLock) {
                return loseTime(before);
            }
        }

        private boolean waitReentrantLock() {
            var before = Thread.currentThread().toString();
            reentrantLock.lock();
            var result = loseTime(before);
            reentrantLock.unlock();
            return result;
        }

        private boolean loseTime(String before) {
            var after = Thread.currentThread().toString();
            logger.debug("Thread before lock: " + before);
            logger.debug("Thread after lock: " + after);
            var start = Instant.now();
            while (start.until(Instant.now(), ChronoUnit.MILLIS) <= 250) {
                // Just to lose a bit of time
            }
            return before.equals(after);
        }
    }
}
