package com.boldfaced7.fxexchange.exchange.application.service.sell.deposit;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositDetail;

public interface DepositKrwService {
    DepositDetail depositKrw(ExchangeRequest requested);
}
