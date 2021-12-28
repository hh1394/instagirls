package com.instagirls.job;

import com.instagirls.service.InstagramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledJobs {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledJobs.class);

    @Autowired
    private InstagramService instagramService;

    @Scheduled(cron = "0 0 4 * * ?")
    public void dailyGirlsUpdate() {
        LOGGER.info("Started updating existing accounts!");
        instagramService.updateGirls();
        LOGGER.info("Done updating existing accounts!");
    }

}
