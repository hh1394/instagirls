package com.instagirls.telegram;

import lombok.Data;

import java.util.Set;

@Data
public class TelegramPost {

    private String girlAccount;
    private String instagramPostId;
    private Set<Media> instagramPostMedias;

    public String getGirlAccountURL() {
        return "https://www.instagram.com/" + this.girlAccount + "/";
    }

}
