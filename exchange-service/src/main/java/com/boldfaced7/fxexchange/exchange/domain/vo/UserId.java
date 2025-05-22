package com.boldfaced7.fxexchange.exchange.domain.vo;

public record UserId(String value) {
    public UserId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("userId는 비어 있을 수 없습니다.");
    }
} 