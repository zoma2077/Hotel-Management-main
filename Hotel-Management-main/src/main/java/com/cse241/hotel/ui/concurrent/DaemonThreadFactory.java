package com.cse241.hotel.ui.concurrent;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates daemon threads with a readable name prefix.
 *
 * <p>Daemon threads ensure background JavaFX services do not prevent JVM shutdown.</p>
 */
public final class DaemonThreadFactory implements ThreadFactory {
    private final String namePrefix;
    private final AtomicInteger counter = new AtomicInteger(1);

    public DaemonThreadFactory(String namePrefix) {
        this.namePrefix = Objects.requireNonNull(namePrefix, "namePrefix is required.");
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, namePrefix + "-" + counter.getAndIncrement());
        t.setDaemon(true);
        return t;
    }
}

