package com.future94.swallow.common.thread;

import java.util.concurrent.TimeUnit;

/**
 * @author weilai
 */
public class ThreadUtils {

    /**
     * sleep current thread.
     *
     * @param timeUnit the time unit
     * @param time     the time
     */
    public static void sleep(final TimeUnit timeUnit, final int time) {
        try {
            timeUnit.sleep(time);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

}