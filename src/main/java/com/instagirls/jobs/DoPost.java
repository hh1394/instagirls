package com.instagirls.jobs;

import com.instagirls.instagram.InstagramService;
import com.instagirls.telegram.TelegramPost;
import com.instagirls.telegram.TelegramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoPost implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DoPost.class);

    @Override
    public void run() {
        LOGGER.info("Executing DoPost()..");
        TelegramPost telegramPost = new InstagramService().generatePost();
        new TelegramService().postToTelegram(telegramPost);

    }
}
