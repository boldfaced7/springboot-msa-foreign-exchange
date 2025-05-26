package com.boldfaced7.fxexchange.exchange.application.service.sell.deposit;

import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public interface DelayKrwDepositCheckService {
    void delayKrwDepositCheck(RequestId requestId, Count count);
}
