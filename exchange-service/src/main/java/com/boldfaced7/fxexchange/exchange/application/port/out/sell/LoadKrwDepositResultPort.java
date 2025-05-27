package com.boldfaced7.fxexchange.exchange.application.port.out.sell;

import com.boldfaced7.fxexchange.exchange.domain.vo.DepositResult;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

public interface LoadKrwDepositResultPort {
    DepositResult loadKrwDepositResult(ExchangeId exchangeId);
}
