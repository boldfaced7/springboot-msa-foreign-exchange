package com.boldfaced7.fxexchange.exchange.application.service.sell.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalDetail;

public interface WithdrawFxService {
    WithdrawalDetail withdrawFx(ExchangeRequest withdrawalRequested);
}
