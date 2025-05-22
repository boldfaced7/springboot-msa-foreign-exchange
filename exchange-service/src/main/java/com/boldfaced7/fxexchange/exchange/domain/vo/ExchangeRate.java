package com.boldfaced7.fxexchange.exchange.domain.vo;

public record ExchangeRate(double value) {
    public ExchangeRate {
        if (value <= 0) throw new IllegalArgumentException("환율은 0보다 커야 합니다.");
    }
} 