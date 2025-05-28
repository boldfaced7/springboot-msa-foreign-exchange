package com.boldfaced7.fxexchange.exchange.domain.event.buy;

import com.boldfaced7.fxexchange.exchange.domain.event.LoggedDomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.LocalDateTime;

public record KrwWithdrawalCompleted(
        RequestId requestId,
        LocalDateTime raisedAt
) implements LoggedDomainEvent {
    public KrwWithdrawalCompleted(RequestId requestId) {
        this(requestId, LocalDateTime.now());
    }
}
