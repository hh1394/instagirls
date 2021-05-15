package com.instagirls.exception;

import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import lombok.AllArgsConstructor;
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
