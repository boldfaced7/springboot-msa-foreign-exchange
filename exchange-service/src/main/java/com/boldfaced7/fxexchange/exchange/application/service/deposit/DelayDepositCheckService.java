package com.boldfaced7.fxexchange.exchange.application.service.deposit;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

public interface DelayDepositCheckService {
    void delayDepositCheck(ExchangeId exchangeId, Count count, Direction direction);
}
