package com.instagirls.util;

import com.instagirls.exception.SleepFailedException;

import java.util.concurrent.TimeUnit;

public class ThreadUtil {

    public static void sleepMinutes(final int minutes) {
        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(minutes));
        } catch (final InterruptedException interruptedException) {
            throw new SleepFailedException("Login sleep failed.", interruptedException);
        }
    }

    public static void sleepSeconds(final int seconds) {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
        } catch (final InterruptedException interruptedException) {
            throw new SleepFailedException("Login sleep failed.", interruptedException);
        }
    }

}
