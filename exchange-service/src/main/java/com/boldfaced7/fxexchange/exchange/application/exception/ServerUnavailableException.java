package com.boldfaced7.fxexchange.exchange.application.exception;

public class ServerUnavailableException extends RuntimeException {
    public ServerUnavailableException(Exception e) {
        super(e);
    }

    public ServerUnavailableException(String message) {
        super(message);
    }

}
