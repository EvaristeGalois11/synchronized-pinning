package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WaitingManager {
    private static final Logger logger = LoggerFactory.getLogger(WaitingManager.class);

    public void startWaiting(boolean useSynchronized) {
        try (var executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                futures.add(executorService.submit(new Waiting(useSynchronized)));
            }
            if (futures.stream().allMatch(this::eval)) {
                logger.info("Every virtual thread seem to be pinned to its carrier thread");
            } else {
                logger.info("There is at least one virtual thread that unmounted from its carrier thread");
            }
        }
    }

    private boolean eval(Future<Boolean> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
