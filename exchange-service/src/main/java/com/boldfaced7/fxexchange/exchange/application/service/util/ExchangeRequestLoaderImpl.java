package com.boldfaced7.fxexchange.exchange.application.service.util;

import com.boldfaced7.fxexchange.exchange.application.port.out.LoadExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangeRequestLoaderImpl implements ExchangeRequestLoader {

    private final LoadExchangeRequestPort loadExchangeRequestPort;

    @Override
    public ExchangeRequest loadExchangeRequest(RequestId requestId) {
        return loadExchangeRequestPort.loadByRequestId(requestId);
    }

    @Override
    public ExchangeRequest loadExchangeRequest(ExchangeId exchangeId) {
        return loadExchangeRequestPort.loadByExchangeId(exchangeId);
    }
}
