package com.boldfaced7.fxexchange.exchange.application.port.out.sell;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositResult;

public interface RequestKrwDepositPort {
    DepositResult depositKrw(ExchangeRequest requested);
}
