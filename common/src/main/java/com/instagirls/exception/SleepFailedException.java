package com.instagirls.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SleepFailedException extends RuntimeException {

    private String message;
    private InterruptedException reason;

    public SleepFailedException(final String message, final InterruptedException reason) {
        super(message, reason);
        this.message = message;
        this.reason = reason;
    }

}
