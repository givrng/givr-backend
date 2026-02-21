package com.backend.givr.shared.exceptions;
/**
 * Authenticating with 3rd part service failed*/
public class ApiAuthenticationException extends RuntimeException {
    public ApiAuthenticationException(String message) {
        super(message);
    }
}
