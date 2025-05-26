package com.boldfaced7.fxexchange.exchange.domain.event.buy;

import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.LocalDateTime;

public record BuyingStarted(
        RequestId requestId,
        LocalDateTime raisedAt
) {
    public BuyingStarted(RequestId requestId) {
        this(requestId, LocalDateTime.now());
    }
}
