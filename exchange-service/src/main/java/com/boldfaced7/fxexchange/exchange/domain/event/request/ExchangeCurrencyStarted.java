package com.boldfaced7.fxexchange.exchange.domain.event.request;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.LocalDateTime;

public record ExchangeCurrencyStarted(
        Direction direction,
        RequestId requestId,
        ExchangeId exchangeId,
        LocalDateTime raisedAt
) implements DomainEvent {
    public ExchangeCurrencyStarted(RequestId requestId, ExchangeId exchangeId, Direction direction) {
        this(direction, requestId, exchangeId, LocalDateTime.now());
    }
}
