package com.cse241.hotel.ui.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FxExecutors {
    private FxExecutors() {
    }

    public static ExecutorService newSingleDaemonExecutor(String threadNamePrefix) {
        return Executors.newSingleThreadExecutor(new DaemonThreadFactory(threadNamePrefix));
    }
}

