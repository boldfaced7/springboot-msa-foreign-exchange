package com.boldfaced7.fxexchange.exchange.domain.vo.exchange;

import com.boldfaced7.fxexchange.exchange.domain.exception.InvalidAmountException;

public record QuoteAmount(int value) {
    public QuoteAmount {
        if (value < 0) 
            throw new InvalidAmountException("상대 통화 금액은 0 이상이어야 합니다.");
    }
} 