package com.instagirls.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstagramPostDTO {

    private String account;
    private String accountURL;
    private String postCode;
    private List<String> mediaURLs;

    public String getAccountURL() {
        return "https://www.instagram.com/" + account + "/";
    }

}
