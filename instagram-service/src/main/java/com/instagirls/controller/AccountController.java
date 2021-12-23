package com.instagirls.controller;

import com.instagirls.service.InstagramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/account")
public class AccountController {

    @Autowired
    private InstagramService instagramService;

    @PostMapping("/{username}")
    public void loadAccount(@PathVariable final String username) {
        instagramService.loadNewAccount(username);
    }


    @DeleteMapping("/{username}")
    public void disableAccount(@PathVariable final String username) {
        instagramService.disableAccount(username);
    }

}
