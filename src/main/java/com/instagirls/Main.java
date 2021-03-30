package com.instagirls;

import com.instagirls.jobs.DoGetUpdates;
import com.instagirls.jobs.DoPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) {
        LOGGER.info("Program started!");
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(new DoPost(), 0, 1, TimeUnit.DAYS);
        scheduler.scheduleAtFixedRate(new DoGetUpdates(), 0, 5, TimeUnit.MINUTES);
    }
}
