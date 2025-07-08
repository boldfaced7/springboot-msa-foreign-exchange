package com.boldfaced7.fxexchange.exchange.domain.vo;

import com.boldfaced7.fxexchange.exchange.domain.model.Deposit;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.model.Withdrawal;

public record ExchangeDetail(
        ExchangeRequest exchangeRequest,
        Withdrawal withdrawal,
        Deposit deposit
) {
    public ExchangeDetail(
            WithdrawalDetail withdrawalDetail,
            DepositDetail depositDetail,
            ExchangeRequest exchangeRequest
    ) {
        this(
                exchangeRequest,
                withdrawalDetail.withdrawal(),
                depositDetail.deposit()
        );
    }
}
