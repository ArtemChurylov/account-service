package com.example.finmid.exception;

import static java.lang.String.format;

public class AccountNotFoundException extends RuntimeException {

    private static final String MESSAGE = "Account not fond for id: %d";

    public AccountNotFoundException(Long id) {
        super(format(MESSAGE, id));
    }

    public AccountNotFoundException(Long id, Throwable cause) {
        super(format(MESSAGE, id), cause);
    }
}
