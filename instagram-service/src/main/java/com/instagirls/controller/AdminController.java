package com.instagirls.controller;

import com.instagirls.service.InstagramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private InstagramService instagramService;

    @DeleteMapping("/accounts/{username}")
    public void removeAccount(@PathVariable final String username) {
        instagramService.deleteAccount(username);
    }

}
