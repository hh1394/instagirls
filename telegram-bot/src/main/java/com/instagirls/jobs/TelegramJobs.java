package com.instagirls.jobs;

import com.instagirls.service.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TelegramJobs {

    @Autowired
    private TelegramService telegramService;

    @Scheduled(cron = "0 0 8 * * ?")
    public void postDailyGirl() {
        telegramService.sendDailyGirl();
    }

}
