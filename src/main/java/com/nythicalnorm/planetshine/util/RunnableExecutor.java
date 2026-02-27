package com.nythicalnorm.planetshine.util;

import java.util.concurrent.ConcurrentLinkedQueue;

public class RunnableExecutor {
    private final ConcurrentLinkedQueue<Runnable> tasks;

    public RunnableExecutor() {
        this.tasks = new ConcurrentLinkedQueue<>();
    }

    public void addRun(Runnable runnable) {
        tasks.add(runnable);
    }

    public void executeAll() {
        Runnable runnable;

        while ((runnable = tasks.poll()) != null) {
            runnable.run();
        }
    }
}
