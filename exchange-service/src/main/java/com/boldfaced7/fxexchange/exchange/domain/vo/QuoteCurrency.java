package com.boldfaced7.fxexchange.exchange.domain.vo;

import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;

public record QuoteCurrency(CurrencyCode value) {
    public QuoteCurrency {
        if (value == null) throw new IllegalArgumentException("상대 통화는 null일 수 없습니다.");
    }
} 