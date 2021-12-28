package com.instagirls.controller;

import com.instagirls.service.InstagramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
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
