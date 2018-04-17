package com.trend.lazyinject.lib.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by swift_gan on 2018/4/17.
 */

public class ThreadPool {

    private ExecutorService executorService;

    public static ThreadPool DEFAULT = new ThreadPool();

    static {
        DEFAULT.poolstart();
    }

    public Future submit(Runnable command) {
        return executorService.submit(command);
    }


    public Future submit(Callable r) {
        return executorService.submit(r);
    }

    public <T> Future submit(Runnable task, T result) {
        return executorService.submit(task,result);
    }

    public void poolstart() {
        ThreadFactory factory = new WorkThreadFactory("thread-pool", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<Runnable>();
        executorService = new ThreadPoolExecutor(1, 8, 10, TimeUnit.SECONDS, workQueue, factory);
    }

    public void poolstop() {
        executorService.shutdown();
    }
}
