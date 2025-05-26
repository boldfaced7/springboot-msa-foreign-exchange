package com.boldfaced7.fxexchange.exchange.application.service.buy.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public interface CheckKrwWithdrawalService {
    void checkKrwWithdrawal(RequestId requestId, Count count);
}
