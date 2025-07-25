package com.boldfaced7.fxexchange.exchange.domain.vo.exchange;

import com.boldfaced7.fxexchange.exchange.domain.exception.InvalidAmountException;

public record ExchangeRate(double value) {
    public ExchangeRate {
        if (value <= 0) 
            throw new InvalidAmountException("환율은 0보다 커야 합니다.");
    }
} 