package com.boldfaced7.fxexchange.exchange.application.service.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalDetail;

public interface WithdrawService {
    WithdrawalDetail withdraw(ExchangeRequest requested);
}
