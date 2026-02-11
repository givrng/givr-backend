package com.backend.givr.shared.exceptions;

public class MaxApplicantsReachedException extends RuntimeException {
    public MaxApplicantsReachedException(String message) {
        super(message);
    }
}
