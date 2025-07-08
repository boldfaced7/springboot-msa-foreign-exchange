package com.boldfaced7.fxexchange.exchange.application.port.out.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.model.Withdrawal;

public interface RequestWithdrawalPort {
    Withdrawal withdraw(ExchangeRequest exchangeRequest);
}
