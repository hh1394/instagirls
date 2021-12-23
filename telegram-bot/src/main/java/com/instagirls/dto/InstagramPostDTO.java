package com.instagirls.dto;

import com.instagirls.model.instagram.InstagramPost;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InstagramPostDTO {

    private InstagramPost instagramPost;
    private String instagramAccountURL;

}
