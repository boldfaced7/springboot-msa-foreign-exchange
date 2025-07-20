package com.boldfaced7.fxexchange.exchange.domain.vo.exchange;

import com.boldfaced7.fxexchange.exchange.domain.exception.InvalidValueException;

public record ExchangeId(String value) {
    public ExchangeId {
        if (value == null || value.isBlank())
            throw new InvalidValueException("exchangeId는 비어 있을 수 없습니다.");
    }
} 