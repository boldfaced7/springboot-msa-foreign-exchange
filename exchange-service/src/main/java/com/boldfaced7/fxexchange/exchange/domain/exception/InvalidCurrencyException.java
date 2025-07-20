package com.boldfaced7.fxexchange.exchange.domain.exception;

public class InvalidCurrencyException extends RuntimeException {
    
    public InvalidCurrencyException(String message) {
        super(message);
    }
    
    public InvalidCurrencyException(String message, Throwable cause) {
        super(message, cause);
    }
} 