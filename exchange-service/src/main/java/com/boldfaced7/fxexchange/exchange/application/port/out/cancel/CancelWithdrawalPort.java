package com.boldfaced7.fxexchange.exchange.application.port.out.cancel;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

public interface CancelWithdrawalPort {
    void cancelWithdrawal(ExchangeId exchangeId, Direction direction);
}
