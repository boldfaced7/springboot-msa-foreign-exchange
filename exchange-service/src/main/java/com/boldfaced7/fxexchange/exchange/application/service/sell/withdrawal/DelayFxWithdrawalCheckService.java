package com.boldfaced7.fxexchange.exchange.application.service.sell.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public interface DelayFxWithdrawalCheckService {
    void delayFxWithdrawalCheck(RequestId requestId, Count count);
}
