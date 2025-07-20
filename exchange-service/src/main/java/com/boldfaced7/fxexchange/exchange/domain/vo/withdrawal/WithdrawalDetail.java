package com.boldfaced7.fxexchange.exchange.domain.vo.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.model.Withdrawal;

public record WithdrawalDetail(
        ExchangeRequest exchangeRequest,
        Withdrawal withdrawal
) {}
