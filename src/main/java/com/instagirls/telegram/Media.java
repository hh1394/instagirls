package com.instagirls.telegram;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Media {

    private String url;
    private MediaType type;

}
