package com.boldfaced7.fxexchange.exchange.domain.event.exchange;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.LocalDateTime;

public record ExchangeCurrencyFailed(
        Direction direction,
        RequestId requestId,
        ExchangeId exchangeId,
        LocalDateTime raisedAt
) implements DomainEvent {
    public ExchangeCurrencyFailed(RequestId requestId, ExchangeId exchangeId, Direction direction) {
        this(direction, requestId, exchangeId, LocalDateTime.now());
    }
}
