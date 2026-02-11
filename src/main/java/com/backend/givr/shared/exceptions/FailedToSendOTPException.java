package com.backend.givr.shared.exceptions;

public class FailedToSendOTPException extends RuntimeException {
    public FailedToSendOTPException(String message) {
        super(message);
    }
}
