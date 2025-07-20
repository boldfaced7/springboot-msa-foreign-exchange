package com.boldfaced7.fxexchange.exchange.domain.event.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;

import java.time.LocalDateTime;

public record WithdrawalAttemptExhausted(
        Direction direction,
        RequestId requestId,
        ExchangeId exchangeId,
        LocalDateTime raisedAt
) implements DomainEvent {
    public WithdrawalAttemptExhausted(RequestId requestId, ExchangeId exchangeId, Direction direction) {
        this(direction, requestId, exchangeId, LocalDateTime.now());
    }
}
