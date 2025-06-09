package com.boldfaced7.fxexchange.exchange.domain.event.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.LocalDateTime;

public record WithdrawalResultUnknown(
        RequestId requestId,
        ExchangeId exchangeId,
        Direction direction,
        Count count,
        LocalDateTime raisedAt
) implements DomainEvent {
    public WithdrawalResultUnknown(RequestId requestId, ExchangeId exchangeId, Direction direction, Count count) {
        this(requestId, exchangeId, direction, count, LocalDateTime.now());
    }
}
