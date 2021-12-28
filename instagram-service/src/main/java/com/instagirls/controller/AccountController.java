package com.instagirls.controller;

import com.instagirls.service.InstagramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private InstagramService instagramService;

    @PostMapping
    public void loadAccounts(@RequestParam final List<String> usernames) {
        LOGGER.info(String.format("Uploading %s users!", usernames.size()));
        usernames.forEach(LOGGER::info);
        usernames.forEach(u -> instagramService.loadNewAccount(u));
    }

    @PostMapping("/{username}")
    public void loadAccount(@PathVariable final String username) {
        instagramService.loadNewAccount(username);
    }

    @DeleteMapping("/{username}")
    public void disableAccount(@PathVariable final String username) {
        instagramService.disableAccount(username);
    }

}
