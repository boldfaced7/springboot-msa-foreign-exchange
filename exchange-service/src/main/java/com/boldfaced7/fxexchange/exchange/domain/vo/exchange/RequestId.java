package com.boldfaced7.fxexchange.exchange.domain.vo.exchange;

import com.boldfaced7.fxexchange.exchange.domain.exception.InvalidValueException;

public record RequestId(Long value) {
    public RequestId {
        if (value == null || value < 0) 
            throw new InvalidValueException("ID는 0 이상이어야 합니다.");
    }
} 