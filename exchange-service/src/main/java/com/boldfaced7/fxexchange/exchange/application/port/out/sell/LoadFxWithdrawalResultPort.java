package com.boldfaced7.fxexchange.exchange.application.port.out.sell;

import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalResult;

public interface LoadFxWithdrawalResultPort {
    WithdrawalResult loadFxWithdrawalResult(ExchangeId exchangeId);
}
