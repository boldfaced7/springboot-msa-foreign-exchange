package com.boldfaced7.fxexchange.exchange.application.port.out.buy;

import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalResult;

public interface LoadKrwWithdrawalResultPort {
    WithdrawalResult loadKrwWithdrawalResult(ExchangeId exchangeId);
}
