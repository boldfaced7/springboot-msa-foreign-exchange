package com.boldfaced7.fxexchange.exchange.application.port.out;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public interface LoadExchangeRequestPort {
    ExchangeRequest loadByRequestId(RequestId requestId);
    ExchangeRequest loadByExchangeId(ExchangeId exchangeId);
}
