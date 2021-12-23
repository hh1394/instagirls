package com.instagirls.exception;

import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoginFailedException extends RuntimeException {

    private String message;
    private IGLoginException reason;

    public LoginFailedException(final String message, final IGLoginException reason) {
        super(message, reason);
        this.message = message;
        this.reason = reason;
    }
}
