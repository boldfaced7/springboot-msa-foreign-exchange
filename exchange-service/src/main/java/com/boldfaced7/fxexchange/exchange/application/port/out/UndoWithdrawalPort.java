package com.boldfaced7.fxexchange.exchange.application.port.out;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

public interface UndoWithdrawalPort {
    void undoWithdrawal(ExchangeId exchangeId);
    Direction direction();
}
