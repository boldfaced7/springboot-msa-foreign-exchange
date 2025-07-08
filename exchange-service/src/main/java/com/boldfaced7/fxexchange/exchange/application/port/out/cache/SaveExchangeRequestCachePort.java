package com.boldfaced7.fxexchange.exchange.application.port.out.cache;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;

public interface SaveExchangeRequestCachePort {
    void save(ExchangeRequest exchangeRequest);
}
