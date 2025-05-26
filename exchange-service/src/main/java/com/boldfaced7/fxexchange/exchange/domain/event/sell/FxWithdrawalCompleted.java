package com.boldfaced7.fxexchange.exchange.domain.event.sell;

import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.LocalDateTime;

public record FxWithdrawalCompleted(
        RequestId requestId,
        LocalDateTime raisedAt
) {
    public FxWithdrawalCompleted(RequestId requestId) {
        this(requestId, LocalDateTime.now());
    }
}
