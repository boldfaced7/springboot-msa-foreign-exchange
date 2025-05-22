package com.boldfaced7.fxexchange.exchange.application.port.out;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawId;

import java.util.Optional;

public interface WithdrawFxPort {
    Optional<WithdrawId> withdrawFx(ExchangeRequest exchangeRequest);
}
