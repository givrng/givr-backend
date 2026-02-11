package com.backend.givr.shared.exceptions;

public class CredentialsChangedException extends RuntimeException {
    public CredentialsChangedException(String message) {
        super(message);
    }
}
