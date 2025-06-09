package com.boldfaced7.fxexchange.exchange.application.service.util.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.PublishExchangeEventPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangeEventPublisherImpl implements ExchangeEventPublisher {

    private final ExchangeRequestLoader exchangeRequestLoader;
    private final PublishExchangeEventPort publishExchangeEventPort;


    @Override
    public void publishEvents(ExchangeRequest exchangeRequest, SimpleEventPublisher simpleEventPublisher) {
        simpleEventPublisher.publish(exchangeRequest);
        publishEvents(exchangeRequest);
    }

    @Override
    public void publishEvents(RequestId requestId, SimpleEventPublisher simpleEventPublisher) {
        var requested = exchangeRequestLoader.loadExchangeRequest(requestId);
        simpleEventPublisher.publish(requested);
        publishEvents(requested);
    }

    @Override
    public void publishEvents(ExchangeId exchangeId, SimpleEventPublisher simpleEventPublisher) {
        var requested = exchangeRequestLoader.loadExchangeRequest(exchangeId);
        simpleEventPublisher.publish(requested);
        publishEvents(requested);
    }

    @Override
    public <T> void publishEvents(ExchangeRequest exchangeRequest, ParamEventPublisher<T> paramEventPublisher, T param) {
        paramEventPublisher.publish(exchangeRequest, param);
        publishEvents(exchangeRequest);
    }

    @Override
    public <T> void publishEvents(RequestId requestId, ParamEventPublisher<T> paramEventPublisher, T param) {
        var requested = exchangeRequestLoader.loadExchangeRequest(requestId);
        paramEventPublisher.publish(requested, param);
        publishEvents(requested);
    }

    @Override
    public <T> void publishEvents(ExchangeId exchangeId, ParamEventPublisher<T> paramEventPublisher, T param) {
        var requested = exchangeRequestLoader.loadExchangeRequest(exchangeId);
        paramEventPublisher.publish(requested, param);
        publishEvents(requested);
    }

    @Override
    public void publishEvents(ExchangeRequest exchangeRequest) {
        var raised = exchangeRequest.pullEvents();
        publishExchangeEventPort.publish(raised);
    }
}