package com.boldfaced7.fxexchange.exchange.application.service.saga.cancel;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

public interface CancelWithdrawalService {
    void cancelWithdrawal(ExchangeId exchangeId, Direction direction);
}
