package com.boldfaced7.fxexchange.exchange.domain.vo.exchange;

import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;
import com.boldfaced7.fxexchange.exchange.domain.exception.InvalidCurrencyException;

public record QuoteCurrency(CurrencyCode value) {
    public QuoteCurrency {
        if (value == null) 
            throw new InvalidCurrencyException("상대 통화는 null일 수 없습니다.");
    }
} 