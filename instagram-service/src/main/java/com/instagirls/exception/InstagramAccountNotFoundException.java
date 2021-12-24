package com.instagirls.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InstagramAccountNotFoundException extends RuntimeException {
    private final String username;

    public InstagramAccountNotFoundException(final String username) {
        this.username = username;
    }

}
