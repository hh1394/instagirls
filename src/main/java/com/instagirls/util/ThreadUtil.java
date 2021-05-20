package com.instagirls.util;

import com.instagirls.exception.SleepFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ThreadUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadUtil.class);

    public static void sleep(final int minutes) {
        try {
            LOGGER.info("Sleeping for a minute.");
            Thread.sleep(TimeUnit.MINUTES.toMillis(minutes));
        } catch (final InterruptedException interruptedException) {
            throw new SleepFailedException("Login sleep failed.", interruptedException);
        }
    }

}
