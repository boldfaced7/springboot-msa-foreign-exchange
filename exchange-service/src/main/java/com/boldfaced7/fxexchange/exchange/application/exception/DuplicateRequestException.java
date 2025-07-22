package com.boldfaced7.fxexchange.exchange.application.exception;

public class DuplicateRequestException extends RuntimeException {
    
    public DuplicateRequestException() {
        super("중복된 요청입니다.");
    }
    
    public DuplicateRequestException(String message) {
        super(message);
    }
    
    public DuplicateRequestException(String message, Throwable cause) {
        super(message, cause);
    }
} 