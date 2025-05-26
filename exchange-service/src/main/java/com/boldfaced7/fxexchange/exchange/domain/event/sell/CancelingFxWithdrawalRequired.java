package com.boldfaced7.fxexchange.exchange.domain.event.sell;

import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

import java.time.LocalDateTime;

public record CancelingFxWithdrawalRequired(
        ExchangeId exchangeId,
        LocalDateTime raisedAt
) {
    public CancelingFxWithdrawalRequired(ExchangeId exchangeId) {
        this(exchangeId, LocalDateTime.now());
    }
}
