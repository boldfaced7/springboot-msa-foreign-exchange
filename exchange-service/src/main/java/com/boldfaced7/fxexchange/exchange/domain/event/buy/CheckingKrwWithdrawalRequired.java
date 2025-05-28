package com.boldfaced7.fxexchange.exchange.domain.event.buy;

import com.boldfaced7.fxexchange.exchange.domain.event.LoggedDomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.LocalDateTime;

public record CheckingKrwWithdrawalRequired(
        RequestId requestId,
        Count count,
        LocalDateTime raisedAt
) implements LoggedDomainEvent {
    public CheckingKrwWithdrawalRequired(RequestId requestId) {
        this(requestId, Count.zero(), LocalDateTime.now());
    }

    public CheckingKrwWithdrawalRequired(RequestId requestId, Count count) {
        this(requestId, count, LocalDateTime.now());
    }
}
