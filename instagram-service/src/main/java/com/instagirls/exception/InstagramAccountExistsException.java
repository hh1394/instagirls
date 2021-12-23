package com.instagirls.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class InstagramAccountExistsException extends RuntimeException {

    private final String username;

    public InstagramAccountExistsException(final String username) {
        this.username = username;
    }
}
