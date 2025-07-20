package com.boldfaced7.fxexchange.exchange.domain.vo.deposit;

import com.boldfaced7.fxexchange.exchange.domain.model.Deposit;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;

public record DepositDetail(
        ExchangeRequest exchangeRequest,
        Deposit deposit
) {}
