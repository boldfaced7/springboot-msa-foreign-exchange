package com.boldfaced7.fxexchange.exchange.domain.vo;

import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;

public record BaseCurrency(CurrencyCode value) {
    public BaseCurrency {
        if (value == null) throw new IllegalArgumentException("기준 통화는 null일 수 없습니다.");
    }
} 