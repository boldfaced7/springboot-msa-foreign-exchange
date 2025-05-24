package com.boldfaced7.fxexchange.exchange.application.service.util;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public interface ExchangeRequestLoader {
    ExchangeRequest loadExchangeRequest(RequestId requestId);
    ExchangeRequest loadExchangeRequest(ExchangeId exchangeId);
}
