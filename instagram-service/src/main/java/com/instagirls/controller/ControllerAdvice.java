package com.instagirls.controller;

import com.instagirls.exception.InstagramAccountExistsException;
import com.instagirls.exception.InstagramAccountNotFoundException;
import com.instagirls.exception.NoActiveAccountException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {

    @ResponseBody
    @ExceptionHandler(InstagramAccountExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    String accountExistsHandler(InstagramAccountExistsException ex) {
        return ex.getUsername();
    }

    @ResponseBody
    @ExceptionHandler(InstagramAccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String accountNotFoundHandler(InstagramAccountNotFoundException ex) {
        return ex.getUsername();
    }

    @ResponseBody
    @ExceptionHandler(NoActiveAccountException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String noActiveAccountHandler(NoActiveAccountException ex) {
        return "No active account.";
    }

}
