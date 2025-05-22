package com.boldfaced7.fxexchange.exchange.domain.vo;

public record WithdrawId(String value) {
    public WithdrawId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("출금 트랜잭션 ID는 비어 있을 수 없습니다.");
    }
} 