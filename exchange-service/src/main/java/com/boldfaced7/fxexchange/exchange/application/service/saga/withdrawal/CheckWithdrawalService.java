package com.boldfaced7.fxexchange.exchange.application.service.saga.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public interface CheckWithdrawalService {
    void checkWithdrawal(RequestId requestId, Count count);

}
