package com.boldfaced7.fxexchange.exchange.domain.event.cancel;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;
import com.boldfaced7.fxexchange.exchange.domain.vo.cancel.WithdrawalCancelId;

import java.time.LocalDateTime;

public record WithdrawalCancelSucceeded(
        Direction direction,
        RequestId requestId,
        ExchangeId exchangeId,
        WithdrawalCancelId withdrawalCancelId,
        LocalDateTime raisedAt
) implements DomainEvent {
    public WithdrawalCancelSucceeded(RequestId requestId, ExchangeId exchangeId, WithdrawalCancelId withdrawalCancelId, Direction direction) {
        this(direction, requestId, exchangeId, withdrawalCancelId, LocalDateTime.now());
    }
}
