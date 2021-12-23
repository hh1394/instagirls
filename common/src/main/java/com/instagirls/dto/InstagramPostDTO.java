package com.instagirls.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InstagramPostDTO {

    private String account;
    private String postCode;
    private List<String> mediaURLs;

    public String getAccountURL() {
        return "https://www.instagram.com/" + account + "/";
    }

}
