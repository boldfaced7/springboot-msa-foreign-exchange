package com.boldfaced7.fxexchange.exchange.domain.event.buy;

import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.LocalDateTime;

public record CheckingFxDepositRequired(
        RequestId requestId,
        Count count,
        LocalDateTime raisedAt
) {
    public CheckingFxDepositRequired(RequestId requestId) {
        this(requestId, Count.zero(), LocalDateTime.now());
    }

    public CheckingFxDepositRequired(RequestId requestId, Count count) {
        this(requestId, count, LocalDateTime.now());
    }
}
