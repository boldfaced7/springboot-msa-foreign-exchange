package com.boldfaced7.fxexchange.exchange.application.port.out.buy;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositResult;

public interface RequestFxDepositPort {
    DepositResult depositFx(ExchangeRequest requested);
}
