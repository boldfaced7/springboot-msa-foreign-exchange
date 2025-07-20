package com.boldfaced7.fxexchange.exchange.domain.event.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;
import com.boldfaced7.fxexchange.exchange.domain.vo.withdrawal.WithdrawalId;

import java.time.LocalDateTime;

public record WithdrawalSucceeded(
        Direction direction,
        RequestId requestId,
        ExchangeId exchangeId,
        WithdrawalId withdrawalId,
        LocalDateTime raisedAt
) implements DomainEvent {
    public WithdrawalSucceeded(RequestId requestId, ExchangeId exchangeId, WithdrawalId withdrawalId, Direction direction) {
        this(direction, requestId, exchangeId, withdrawalId, LocalDateTime.now());
    }
}
