package com.boldfaced7.fxexchange.exchange.application.service.saga.deposit;

import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;

public interface CheckDepositService {
    void checkDeposit(RequestId requestId, Count count);
}
