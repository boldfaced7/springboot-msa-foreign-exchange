package com.boldfaced7.fxexchange.exchange.application.service.saga.exchange;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;

public interface CompleteExchangeService {
    ExchangeRequest succeedExchange(ExchangeRequest exchangeRequest);

    ExchangeRequest succeedExchange(RequestId requestId);
    ExchangeRequest failExchange(RequestId requestId);
}
