package com.boldfaced7.fxexchange.exchange.application.service.buy.deposit;

import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public interface DelayFxDepositCheckService {
    void delayFxDepositCheck(RequestId requestId, Count count);
}
