package com.boldfaced7.fxexchange.exchange.domain.event.sell;

import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.LocalDateTime;

public record CheckingFxWithdrawalRequired(
        RequestId requestId,
        Count count,
        LocalDateTime raisedAt
) {
    public CheckingFxWithdrawalRequired(RequestId requestId) {
        this(requestId, Count.zero(), LocalDateTime.now());
    }

    public CheckingFxWithdrawalRequired(RequestId requestId, Count count) {
        this(requestId, count, LocalDateTime.now());
    }
}
