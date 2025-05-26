package com.boldfaced7.fxexchange.exchange.domain.event.buy;

import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.LocalDateTime;

public record DelayingFxDepositCheckRequired(
        RequestId requestId,
        Count count,
        LocalDateTime raisedAt
) {
    public DelayingFxDepositCheckRequired(
            RequestId requestId
    ) {
        this(requestId, Count.zero(), LocalDateTime.now());
    }

    public DelayingFxDepositCheckRequired(RequestId requestId, Count count) {
        this(requestId, count, LocalDateTime.now());
    }
}
