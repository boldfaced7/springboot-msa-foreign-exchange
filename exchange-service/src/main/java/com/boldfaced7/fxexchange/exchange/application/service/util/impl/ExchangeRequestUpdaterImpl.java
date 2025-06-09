package com.boldfaced7.fxexchange.exchange.application.service.util.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.LoadExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangeRequestUpdaterImpl implements ExchangeRequestUpdater {

    private final LoadExchangeRequestPort loadExchangeRequestPort;
    private final UpdateExchangeRequestPort updateExchangeRequestPort;

    @Override
    public ExchangeRequest update(ExchangeRequest exchangeRequest, SimpleRequestUpdater simpleRequestUpdater) {
        simpleRequestUpdater.update(exchangeRequest);
        return updateExchangeRequestPort.update(exchangeRequest);
    }

    @Override
    public ExchangeRequest update(RequestId requestId, SimpleRequestUpdater simpleRequestUpdater) {
        var requested = loadExchangeRequestPort.loadByRequestId(requestId);
        simpleRequestUpdater.update(requested);
        return updateExchangeRequestPort.update(requested);
    }

    @Override
    public ExchangeRequest update(ExchangeId exchangeId, SimpleRequestUpdater simpleRequestUpdater) {
        var requested = loadExchangeRequestPort.loadByExchangeId(exchangeId);
        simpleRequestUpdater.update(requested);
        return updateExchangeRequestPort.update(requested);
    }

    @Override
    public <T> ExchangeRequest update(ExchangeRequest exchangeRequest, ParamRequestUpdater<T> paramRequestUpdater, T param) {
        paramRequestUpdater.update(exchangeRequest, param);
        return updateExchangeRequestPort.update(exchangeRequest);
    }

    @Override
    public <T> ExchangeRequest update(RequestId requestId, ParamRequestUpdater<T> paramRequestUpdater, T param) {
        var requested = loadExchangeRequestPort.loadByRequestId(requestId);
        paramRequestUpdater.update(requested, param);
        return updateExchangeRequestPort.update(requested);
    }

    @Override
    public <T> ExchangeRequest update(ExchangeId exchangeId, ParamRequestUpdater<T> paramRequestUpdater, T param) {
        var requested = loadExchangeRequestPort.loadByExchangeId(exchangeId);
        paramRequestUpdater.update(requested, param);
        return updateExchangeRequestPort.update(requested);
    }


}
