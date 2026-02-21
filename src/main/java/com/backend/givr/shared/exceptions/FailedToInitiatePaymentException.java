package com.backend.givr.shared.exceptions;

public class FailedToInitiatePaymentException extends RuntimeException{
    public FailedToInitiatePaymentException(Throwable cause){
        super(cause);
    }
    public FailedToInitiatePaymentException(String message){
        super(message);
    }
}
