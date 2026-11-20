package com.backend.givr.shared.exceptions;

public class VerificationPaymentProcessedException extends RuntimeException {
    public VerificationPaymentProcessedException(String message) {
        super(message);
    }
}
