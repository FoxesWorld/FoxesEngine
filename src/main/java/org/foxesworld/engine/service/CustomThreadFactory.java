package org.foxesworld.engine.service;

import java.util.concurrent.ThreadFactory;

/**
 * Custom thread factory to create threads with a specific name prefix.
 */
class CustomThreadFactory implements ThreadFactory {
    private final String prefix;
    private int count = 0;

    CustomThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, prefix + "-" + count++);
    }
}