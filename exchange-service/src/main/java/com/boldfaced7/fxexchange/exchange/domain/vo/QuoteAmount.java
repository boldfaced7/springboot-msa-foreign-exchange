package com.boldfaced7.fxexchange.exchange.domain.vo;

public record QuoteAmount(int value) {
    public QuoteAmount {
        if (value < 0) throw new IllegalArgumentException("상대 통화 금액은 0 이상이어야 합니다.");
    }
} 