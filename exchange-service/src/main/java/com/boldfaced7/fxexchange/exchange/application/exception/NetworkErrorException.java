package com.boldfaced7.fxexchange.exchange.application.exception;

public class NetworkErrorException extends RuntimeException {
    public NetworkErrorException(Exception e) {
        super(e);
    }

    public NetworkErrorException(String message) {
        super(message);
    }
}
