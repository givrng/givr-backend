package com.backend.givr.shared.exceptions;

public class InconsistentProjectDatesException extends RuntimeException {
    public InconsistentProjectDatesException(String message) {
        super(message);
    }
}
