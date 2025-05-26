package com.boldfaced7.fxexchange.exchange.application.service.buy.deposit;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositDetail;

public interface DepositFxService {
    DepositDetail depositFx(ExchangeRequest requested);
}
