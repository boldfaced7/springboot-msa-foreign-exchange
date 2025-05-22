package com.boldfaced7.fxexchange.exchange.domain.vo;

public record ExchangeId(String value) {
    public ExchangeId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("exchangeId는 비어 있을 수 없습니다.");
    }

    public ExchangeId() {
        this(String.valueOf(System.currentTimeMillis()));
    }
} 