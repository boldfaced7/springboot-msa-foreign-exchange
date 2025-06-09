package com.boldfaced7.fxexchange.exchange.application.port.out;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalResult;

public interface RequestWithdrawalPort {
    WithdrawalResult withdraw(ExchangeRequest requested);
    Direction direction();
}
