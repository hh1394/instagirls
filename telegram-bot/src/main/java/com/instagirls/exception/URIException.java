package com.instagirls.exception;

import lombok.Data;

import java.net.URISyntaxException;

@Data
public class URIException extends RuntimeException {

    private Throwable cause;

    public URIException(final URISyntaxException e) {
        this.cause = e;
    }
}
