package com.boldfaced7.fxexchange.exchange.application.service.buy.compensate;

import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

public interface CancelKrwWithdrawalService {
    void cancelKrwWithdrawal(ExchangeId exchangeId);
}
