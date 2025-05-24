package com.boldfaced7.fxexchange.exchange.domain.vo;

public record RequestId(Long value) {
    public RequestId {
        if (value == null || value < 0) throw new IllegalArgumentException("ID는 0 이상이어야 합니다.");
    }
} 