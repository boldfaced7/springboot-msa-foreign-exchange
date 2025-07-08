package com.boldfaced7.fxexchange.exchange.domain.event.deposit;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.Duration;
import java.time.LocalDateTime;

public record DepositCheckUnknown(
        Direction direction,
        RequestId requestId,
        ExchangeId exchangeId,
        Count count,
        Duration delay,
        LocalDateTime raisedAt
) implements DomainEvent {
    public DepositCheckUnknown(
            RequestId requestId,
            ExchangeId exchangeId,
            Direction direction,
            Count count,
            Duration delay
    ) {
        this(direction, requestId, exchangeId, count, delay, LocalDateTime.now());
    }
}
