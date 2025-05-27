package com.boldfaced7.fxexchange.exchange.application.port.out.buy;

import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

public interface UndoKrwWithdrawalPort {
    void undoKrwWithdrawn(ExchangeId exchangeId);
}
