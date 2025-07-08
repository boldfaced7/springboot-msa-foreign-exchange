package com.boldfaced7.fxexchange.exchange.application.port.out.exchange;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;

public interface UpdateExchangeRequestPort {
    ExchangeRequest update(ExchangeRequest exchangeRequest);
}
