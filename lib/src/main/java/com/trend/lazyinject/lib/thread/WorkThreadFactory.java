package com.trend.lazyinject.lib.thread;

import android.os.Process;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkThreadFactory implements ThreadFactory {
    private final String name;
    private final int priority;
    private final AtomicInteger number = new AtomicInteger();

    public WorkThreadFactory(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, name + "-" + number.getAndIncrement()) {
            @Override
            public void run() {
                Process.setThreadPriority(priority);
                super.run();
            }
        };
    }
}
