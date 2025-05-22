package com.boldfaced7.fxexchange.exchange.application.port.out;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;

public interface UpdateExchangeRequestPort {
    ExchangeRequest update(ExchangeRequest exchangeRequest);
}
