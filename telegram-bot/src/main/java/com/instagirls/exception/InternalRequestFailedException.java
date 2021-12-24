package com.instagirls.exception;

import lombok.Data;

@Data
public class InternalRequestFailedException extends RuntimeException {

    private Integer statusCode;

    public InternalRequestFailedException(final int statusCode) {
        this.statusCode = statusCode;
    }
}
