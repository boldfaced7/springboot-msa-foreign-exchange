package com.boldfaced7.fxexchange.exchange.application.port.out.exchange;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.util.Optional;

public interface LoadExchangeRequestPort {
    Optional<ExchangeRequest> loadByRequestIdForUpdate(RequestId requestId);
}
