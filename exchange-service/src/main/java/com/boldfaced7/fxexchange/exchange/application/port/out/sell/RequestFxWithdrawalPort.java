package com.boldfaced7.fxexchange.exchange.application.port.out.sell;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalResult;

public interface RequestFxWithdrawalPort {
    WithdrawalResult withdrawFx(ExchangeRequest requested);
}
