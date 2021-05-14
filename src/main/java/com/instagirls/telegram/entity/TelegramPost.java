package com.instagirls.telegram.entity;

import lombok.Data;

import java.util.Set;

@Data
public class TelegramPost {

    private String girlAccount;
    private String instagramPostId;
    private String caption;
    private Set<Media> instagramPostMedias;

    public String getGirlAccountURL() {
        return "https://www.instagram.com/" + this.girlAccount + "/";
    }
}
