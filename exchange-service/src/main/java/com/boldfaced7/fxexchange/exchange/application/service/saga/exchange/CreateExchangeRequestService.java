package com.boldfaced7.fxexchange.exchange.application.service.saga.exchange;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;

public interface CreateExchangeRequestService {
    ExchangeRequest createExchangeRequest(ExchangeRequest exchangeRequest);
}
