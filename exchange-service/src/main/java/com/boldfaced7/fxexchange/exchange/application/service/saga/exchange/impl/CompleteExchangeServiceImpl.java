package com.boldfaced7.fxexchange.exchange.application.service.saga.exchange.impl;

import com.boldfaced7.fxexchange.exchange.application.port.aop.DistributedLock;
import com.boldfaced7.fxexchange.exchange.application.port.aop.Idempotent;
import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.exchange.LoadExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.exchange.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.service.saga.exchange.CompleteExchangeService;
import com.boldfaced7.fxexchange.exchange.domain.exception.ExchangeRequestNotFoundException;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompleteExchangeServiceImpl implements CompleteExchangeService {

    private final LoadExchangeRequestPort loadExchangeRequestPort;
    private final UpdateExchangeRequestPort updateExchangeRequestPort;
    private final PublishEventPort publishEventPort;

    @Override
    @Transactional
    public ExchangeRequest succeedExchange(ExchangeRequest exchangeRequest) {
        publishExchangeEvent(exchangeRequest, true);
        return updateExchangeRequestPort.update(exchangeRequest);
    }

    @Override
    @Transactional
    @Idempotent(prefix = "exchange:succeed:", key = "#requestId")
    public ExchangeRequest succeedExchange(RequestId requestId) {
        return completeExchange(requestId, true);
    }

    @Override
    @Transactional
    @Idempotent(prefix = "exchange:fail:", key = "#requestId")
    public ExchangeRequest failExchange(RequestId requestId) {
        return completeExchange(requestId, false);
    }

    private ExchangeRequest completeExchange(RequestId requestId, boolean succeeded) {
        var exchange = getExchange(requestId);
        publishExchangeEvent(exchange, succeeded);
        return updateExchangeRequestPort.update(exchange);
    }

    private ExchangeRequest getExchange(RequestId requestId) {
        return loadExchangeRequestPort.loadByRequestIdForUpdate(requestId)
                .orElseThrow(() -> new ExchangeRequestNotFoundException(requestId));
    }

    private void publishExchangeEvent(ExchangeRequest exchange, boolean isSucceeded) {
        exchange.completeExchange(isSucceeded);
        publishEventPort.publish(exchange);
    }
}
