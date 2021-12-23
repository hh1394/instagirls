package com.instagirls.exception;

public class UnexpectedInstagramException extends RuntimeException {
    public UnexpectedInstagramException(final Throwable throwable) {
        super(throwable);
    }
}
