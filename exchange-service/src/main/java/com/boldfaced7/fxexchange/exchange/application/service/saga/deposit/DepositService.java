package com.boldfaced7.fxexchange.exchange.application.service.saga.deposit;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.deposit.DepositDetail;

public interface DepositService {
    DepositDetail deposit(ExchangeRequest exchange);
}
