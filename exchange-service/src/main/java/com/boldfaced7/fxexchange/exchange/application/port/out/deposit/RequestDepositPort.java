package com.boldfaced7.fxexchange.exchange.application.port.out.deposit;

import com.boldfaced7.fxexchange.exchange.domain.model.Deposit;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;

public interface RequestDepositPort {
    Deposit deposit(ExchangeRequest exchangeRequest);
}
