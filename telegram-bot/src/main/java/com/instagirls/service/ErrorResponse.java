package com.instagirls.service;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ErrorResponse {

    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

}
