package com.instagirls.exception;

import lombok.Data;

@Data
public class UncheckedInterruptedException extends RuntimeException {

    private Throwable cause;

    public UncheckedInterruptedException(final InterruptedException e) {
        this.cause = e;
    }
}
