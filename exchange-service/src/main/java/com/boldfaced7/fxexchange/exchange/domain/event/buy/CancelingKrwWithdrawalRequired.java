package com.boldfaced7.fxexchange.exchange.domain.event.buy;

import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

import java.time.LocalDateTime;

public record CancelingKrwWithdrawalRequired(
        ExchangeId exchangeId,
        LocalDateTime raisedAt
) {
    public CancelingKrwWithdrawalRequired(ExchangeId exchangeId) {
        this(exchangeId, LocalDateTime.now());
    }
}
