package com.boldfaced7.fxexchange.exchange.domain.vo;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;

public record ExchangeDetail(
        ExchangeRequest exchangeRequest,
        WithdrawalResult withdrawalResult,
        DepositResult depositResult
) {
    public ExchangeDetail(
            WithdrawalDetail withdrawalDetail,
            DepositDetail depositDetail
    ) {
        this(
                depositDetail.exchangeRequest(),
                withdrawalDetail.withdrawalResult(),
                depositDetail.depositResult()
        );
    }
}
