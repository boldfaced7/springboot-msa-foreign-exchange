package com.boldfaced7.fxexchange.exchange.domain.event.sell;

import com.boldfaced7.fxexchange.exchange.domain.event.LoggedDomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.LocalDateTime;

public record CancelingFxWithdrawalRequired(
        RequestId requestId,
        ExchangeId exchangeId,
        LocalDateTime raisedAt
) implements LoggedDomainEvent {
    public CancelingFxWithdrawalRequired(RequestId requestId, ExchangeId exchangeId) {
        this(requestId,exchangeId, LocalDateTime.now());
    }
}
