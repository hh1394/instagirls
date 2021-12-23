package com.instagirls.util;

import com.instagirls.exception.SleepFailedException;

import java.util.concurrent.TimeUnit;

public class ThreadUtil {

    public static void sleep(final int minutes) {
        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(minutes));
        } catch (final InterruptedException interruptedException) {
            throw new SleepFailedException("Login sleep failed.", interruptedException);
        }
    }

}
