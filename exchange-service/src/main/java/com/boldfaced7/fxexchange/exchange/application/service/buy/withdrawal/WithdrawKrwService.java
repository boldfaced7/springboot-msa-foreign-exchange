package com.boldfaced7.fxexchange.exchange.application.service.buy.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalDetail;

public interface WithdrawKrwService {
    WithdrawalDetail withdrawKrw(ExchangeRequest withdrawalRequested);
}
