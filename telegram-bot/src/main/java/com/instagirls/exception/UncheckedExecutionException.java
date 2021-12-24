package com.instagirls.exception;

import lombok.Data;

import java.util.concurrent.ExecutionException;

@Data
public class UncheckedExecutionException extends RuntimeException {

    private Throwable cause;

    public UncheckedExecutionException(final ExecutionException e) {

        this.cause = e;
    }
}
