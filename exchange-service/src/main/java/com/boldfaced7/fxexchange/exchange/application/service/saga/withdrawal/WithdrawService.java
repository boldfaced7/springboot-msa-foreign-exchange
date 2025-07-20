package com.boldfaced7.fxexchange.exchange.application.service.saga.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.withdrawal.WithdrawalDetail;

public interface WithdrawService {
    WithdrawalDetail withdraw(ExchangeRequest exchange);
}
