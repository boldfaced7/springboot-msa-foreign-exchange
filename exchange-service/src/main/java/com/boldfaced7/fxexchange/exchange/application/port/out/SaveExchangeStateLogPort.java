package com.boldfaced7.fxexchange.exchange.application.port.out;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeStateLog;

public interface SaveExchangeStateLogPort {
    ExchangeStateLog save(ExchangeStateLog exchangeStateLog);
}
