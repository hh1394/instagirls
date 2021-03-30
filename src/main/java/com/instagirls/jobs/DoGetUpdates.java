package com.instagirls.jobs;

import com.instagirls.telegram.TelegramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DoGetUpdates implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DoGetUpdates.class);

    @Override
    public void run() {
        LOGGER.info("Executing DoGetUpdates()..");
        try {
            new TelegramService().updateChats();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
