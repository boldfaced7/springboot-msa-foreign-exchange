package com.boldfaced7.fxexchange.exchange.application.port.out;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalResult;

public interface LoadWithdrawalResultPort {
    WithdrawalResult loadWithdrawalResult(ExchangeId exchangeId, Direction direction);
}
