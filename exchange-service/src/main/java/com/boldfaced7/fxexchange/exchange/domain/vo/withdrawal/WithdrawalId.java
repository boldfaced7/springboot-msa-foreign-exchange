package com.boldfaced7.fxexchange.exchange.domain.vo.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.exception.InvalidValueException;

public record WithdrawalId(String value) {
    public WithdrawalId {
        if (value == null || value.isBlank()) 
            throw new InvalidValueException("출금 트랜잭션 ID는 비어 있을 수 없습니다.");
    }
} 