package com.boldfaced7.fxexchange.exchange.application.service.sell.compensate;

import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

public interface CancelFxWithdrawalService {
    void cancelFxWithdrawal(ExchangeId exchangeId);
}
