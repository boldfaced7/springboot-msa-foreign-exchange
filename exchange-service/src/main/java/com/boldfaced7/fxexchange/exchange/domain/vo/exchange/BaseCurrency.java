package com.boldfaced7.fxexchange.exchange.domain.vo.exchange;

import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;
import com.boldfaced7.fxexchange.exchange.domain.exception.InvalidCurrencyException;

public record BaseCurrency(CurrencyCode value) {
    public BaseCurrency {
        if (value == null) 
            throw new InvalidCurrencyException("기준 통화는 null일 수 없습니다.");
    }
} 