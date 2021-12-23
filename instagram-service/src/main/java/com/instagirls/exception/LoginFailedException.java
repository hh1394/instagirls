package com.instagirls.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoginFailedException extends RuntimeException {

    private String message;
    private Exception reason;

    public LoginFailedException(final String message, final Exception reason) {
        super(message, reason);
        this.message = message;
        this.reason = reason;
    }
}
