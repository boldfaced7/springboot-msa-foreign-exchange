package com.boldfaced7.fxexchange.exchange.application.port.out.log;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeStateLog;

public interface SaveExchangeStateLogPort {
    ExchangeStateLog save(ExchangeStateLog exchangeStateLog);
}
