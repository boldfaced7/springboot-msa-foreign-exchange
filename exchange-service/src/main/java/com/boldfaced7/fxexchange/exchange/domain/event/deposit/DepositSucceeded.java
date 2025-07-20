package com.boldfaced7.fxexchange.exchange.domain.event.deposit;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.vo.deposit.DepositId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;

import java.time.LocalDateTime;

public record DepositSucceeded(
        Direction direction,
        RequestId requestId,
        ExchangeId exchangeId,
        DepositId depositId,
        LocalDateTime raisedAt
) implements DomainEvent {
    public DepositSucceeded(RequestId requestId, ExchangeId exchangeId, DepositId depositId, Direction direction) {
        this(direction, requestId, exchangeId, depositId, LocalDateTime.now());
    }
}
