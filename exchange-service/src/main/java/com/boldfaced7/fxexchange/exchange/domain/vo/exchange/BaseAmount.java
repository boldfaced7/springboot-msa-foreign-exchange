package com.boldfaced7.fxexchange.exchange.domain.vo.exchange;

import com.boldfaced7.fxexchange.exchange.domain.exception.InvalidAmountException;

public record BaseAmount(int value) {
    public BaseAmount {
        if (value < 0) 
            throw new InvalidAmountException("기준 통화 금액은 0 이상이어야 합니다.");
    }
} 