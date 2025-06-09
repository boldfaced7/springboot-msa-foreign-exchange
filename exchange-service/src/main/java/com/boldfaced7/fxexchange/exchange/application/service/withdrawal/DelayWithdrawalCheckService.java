package com.boldfaced7.fxexchange.exchange.application.service.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

public interface DelayWithdrawalCheckService {
    void delayWithdrawalCheck(ExchangeId exchangeId, Count count, Direction direction);
}
