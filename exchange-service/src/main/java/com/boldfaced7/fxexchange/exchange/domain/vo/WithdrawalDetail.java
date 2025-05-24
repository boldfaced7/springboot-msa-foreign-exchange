package com.boldfaced7.fxexchange.exchange.domain.vo;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;

public record WithdrawalDetail(
        ExchangeRequest exchangeRequest,
        WithdrawalResult withdrawalResult
) {
}
