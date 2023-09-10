package com.example.finance.controller;

import com.example.finance.exception.AccountNotFoundException;
import com.example.finance.exception.InsufficientFundsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({AccountNotFoundException.class, InsufficientFundsException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String serverExceptionHandler(RuntimeException ex) {
        log.error(ex.getMessage(), ex);
        return ex.getMessage();
    }
}
