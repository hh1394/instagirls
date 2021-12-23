package com.instagirls.controller;

import com.instagirls.exception.InstagramAccountExistsException;
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
    String employeeNotFoundHandler(InstagramAccountExistsException ex) {
        return ex.getUsername();
    }

}
