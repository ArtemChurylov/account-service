package com.example.finmid.exception;

import static java.lang.String.format;

public class InsufficientFundsException extends RuntimeException {
    private static final String MESSAGE = "Insufficient funds for account: %d";

    public InsufficientFundsException(Long id) {
        super(format(MESSAGE, id));
    }
}
