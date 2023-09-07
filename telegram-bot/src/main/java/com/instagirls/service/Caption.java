package com.instagirls.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Caption {

    private String quote;
    private String author;
    private String category;

    public String buildCaption() {
        return "\""+quote+"\" - " + author;
    }

}
