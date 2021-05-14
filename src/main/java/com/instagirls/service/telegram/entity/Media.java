package com.instagirls.service.telegram.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Media {

    private String url;
    private MediaType type;

}
