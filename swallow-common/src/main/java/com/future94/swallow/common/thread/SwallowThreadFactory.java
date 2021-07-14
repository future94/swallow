package com.future94.swallow.common.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author weilai
 */
public class SwallowThreadFactory implements ThreadFactory {

    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("swallow");

    private static final AtomicInteger THREAD_NUMBER = new AtomicInteger(1);

    private String prefix;

    private int newPriority;

    private boolean daemon;

    private SwallowThreadFactory() {
    }

    private SwallowThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    public SwallowThreadFactory(String prefix, int newPriority, boolean daemon) {
        this.prefix = prefix;
        this.newPriority = newPriority;
        this.daemon = daemon;
    }

    public static SwallowThreadFactory create(String prefix) {
        return create(prefix, false, Thread.NORM_PRIORITY);
    }

    public static SwallowThreadFactory create(String prefix, boolean daemon) {
        return create(prefix, daemon, Thread.NORM_PRIORITY);
    }

    public static SwallowThreadFactory create(String prefix, boolean daemon, int newPriority) {
        return new SwallowThreadFactory(prefix, newPriority, daemon);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(THREAD_GROUP, r, THREAD_GROUP.getName() + "-" + prefix + "-" + THREAD_NUMBER.getAndIncrement());
        thread.setDaemon(daemon);
        thread.setPriority(newPriority);
        return thread;
    }
}
