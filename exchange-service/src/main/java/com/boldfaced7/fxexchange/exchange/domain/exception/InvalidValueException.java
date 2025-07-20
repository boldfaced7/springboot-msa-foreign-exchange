package com.boldfaced7.fxexchange.exchange.domain.exception;

public class InvalidValueException extends RuntimeException {
    
    public InvalidValueException(String message) {
        super(message);
    }
    
    public InvalidValueException(String message, Throwable cause) {
        super(message, cause);
    }
} 