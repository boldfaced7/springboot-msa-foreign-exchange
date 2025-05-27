package com.boldfaced7.fxexchange.exchange.application.port.out.buy;

import com.boldfaced7.fxexchange.exchange.domain.vo.DepositResult;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

public interface LoadFxDepositResultPort {
    DepositResult loadFxDepositResult(ExchangeId exchangeId);
}
