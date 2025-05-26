package com.boldfaced7.fxexchange.exchange.domain.event.buy;

import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.LocalDateTime;

public record BuyingCompleted(
        RequestId requestId,
        LocalDateTime raisedAt
) {
    public BuyingCompleted(RequestId requestId) {
        this(requestId, LocalDateTime.now());
    }
}
