package com.boldfaced7.fxexchange.exchange.application.service.saga.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;

public interface CheckWithdrawalService {
    void checkWithdrawal(RequestId requestId, Count count);

}
