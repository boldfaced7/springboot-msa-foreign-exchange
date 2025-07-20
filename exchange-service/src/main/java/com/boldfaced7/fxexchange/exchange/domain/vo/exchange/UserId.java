package com.boldfaced7.fxexchange.exchange.domain.vo.exchange;

import com.boldfaced7.fxexchange.exchange.domain.exception.InvalidValueException;

public record UserId(String value) {
    public UserId {
        if (value == null || value.isBlank()) 
            throw new InvalidValueException("userId는 비어 있을 수 없습니다.");
    }
} 