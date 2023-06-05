package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

public class Waiting implements Callable<Boolean> {
    private static final Logger logger = LoggerFactory.getLogger(Waiting.class);

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
