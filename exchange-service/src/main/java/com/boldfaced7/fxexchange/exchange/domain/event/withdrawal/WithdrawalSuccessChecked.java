package com.boldfaced7.fxexchange.exchange.domain.event.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalId;

import java.time.LocalDateTime;

public record WithdrawalSuccessChecked(
        Direction direction,
        RequestId requestId,
        ExchangeId exchangeId,
        WithdrawalId withdrawalId,
        LocalDateTime raisedAt
) implements DomainEvent {
    public WithdrawalSuccessChecked(RequestId requestId, ExchangeId exchangeId, WithdrawalId withdrawalId, Direction direction) {
        this(direction, requestId, exchangeId, withdrawalId, LocalDateTime.now());
    }
}
