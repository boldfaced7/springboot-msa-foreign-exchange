package com.boldfaced7.fxexchange.exchange.application.exception;

public class ExchangeAlreadyRequestedException extends RuntimeException {
    public ExchangeAlreadyRequestedException() {
        super("이미 환전이 요청되었습니다.");
    }
}
