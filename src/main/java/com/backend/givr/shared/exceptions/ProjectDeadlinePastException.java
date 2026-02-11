package com.backend.givr.shared.exceptions;

public class ProjectDeadlinePastException extends RuntimeException {
    public ProjectDeadlinePastException(String message) {
        super(message);
    }
}
