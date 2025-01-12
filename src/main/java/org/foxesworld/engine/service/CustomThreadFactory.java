package org.foxesworld.engine.service;

import org.apache.logging.log4j.ThreadContext;

import java.util.concurrent.ThreadFactory;

/**
 * Custom thread factory to create threads with a specific name prefix and MDC support.
 */
class CustomThreadFactory implements ThreadFactory {
    private final String prefix;
    private int count = 0;

    CustomThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        int threadNumber = count++;
        String threadName = prefix + '-'+ threadNumber;

        return new Thread(() -> {
            try {
                ThreadContext.put("workerName", threadName);

                r.run();
            } finally {
                // Clear MDC context to avoid leaking data between threads
                ThreadContext.clearAll();
            }
        }, threadName);
    }
}

