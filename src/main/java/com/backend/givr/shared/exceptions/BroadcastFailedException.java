package com.backend.givr.shared.exceptions;

public class BroadcastFailedException extends RuntimeException {
    public BroadcastFailedException(String message) {
        super(message);
    }
}
