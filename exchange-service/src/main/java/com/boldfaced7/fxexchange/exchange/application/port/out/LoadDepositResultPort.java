package com.boldfaced7.fxexchange.exchange.application.port.out;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositResult;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

public interface LoadDepositResultPort {
    DepositResult loadDepositResult(ExchangeId exchangeId, Direction direction);
}
