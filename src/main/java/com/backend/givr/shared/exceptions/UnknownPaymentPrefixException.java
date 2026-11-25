package com.backend.givr.shared.exceptions;

public class UnknownPaymentPrefixException extends RuntimeException {
    public UnknownPaymentPrefixException(String message) {
        super(message);
    }
}
