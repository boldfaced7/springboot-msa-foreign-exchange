package com.boldfaced7.fxexchange.exchange.application.port.out;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositId;

import java.util.Optional;

public interface DepositKrwPort {
    Optional<DepositId> depositKrw(ExchangeRequest exchangeRequest);
}
