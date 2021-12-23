package com.instagirls.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UnsupportedMediaFormatException extends RuntimeException {

    private String message;

    public UnsupportedMediaFormatException(final String message) {
        this.message = message;
    }
}
