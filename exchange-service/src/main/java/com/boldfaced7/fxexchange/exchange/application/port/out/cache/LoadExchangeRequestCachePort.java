package com.boldfaced7.fxexchange.exchange.application.port.out.cache;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;

import java.util.Optional;

public interface LoadExchangeRequestCachePort {
    Optional<ExchangeRequest> loadByRequestId(RequestId requestId);
    Optional<ExchangeRequest> loadByExchangeId(ExchangeId exchangeId);
}
