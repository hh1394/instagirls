package com.instagirls.controller;

import com.instagirls.dto.InstagramPostDTO;
import com.instagirls.service.InstagramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController("/post")
public class PostController {

    @Autowired
    private InstagramService instagramService;

    @GetMapping("/random")
    @ResponseBody
    public InstagramPostDTO getRandomMostLikedPost() {
        return instagramService.getNewMostLikedPostFromRandomAccount();
    }

    @PatchMapping("/{postCode}")
    public void setPosted(@PathVariable final String postCode) {
        instagramService.setPosted(postCode);
    }

    @GetMapping("/account/{username}")
    public InstagramPostDTO getMostLikedPostForAccount(@PathVariable final String username) {
        return instagramService.getNewMostLikedPostFromAccount(username);
    }

}
