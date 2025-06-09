package com.boldfaced7.fxexchange.exchange.application.service.util;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public interface ExchangeEventPublisher {
    void publishEvents(ExchangeRequest exchangeRequest, SimpleEventPublisher simpleEventPublisher);
    void publishEvents(RequestId requestId, SimpleEventPublisher simpleEventPublisher);
    void publishEvents(ExchangeId exchangeId, SimpleEventPublisher simpleEventPublisher);

    <T> void publishEvents(ExchangeRequest exchangeRequest, ParamEventPublisher<T> paramEventPublisher, T param);
    <T> void publishEvents(RequestId requestId, ParamEventPublisher<T> paramEventPublisher, T param);
    <T> void publishEvents(ExchangeId exchangeId, ParamEventPublisher<T> paramEventPublisher, T param);

    void publishEvents(ExchangeRequest exchangeRequest);

        @FunctionalInterface
    interface SimpleEventPublisher {
        void publish(ExchangeRequest exchangeRequest);
    }

    @FunctionalInterface
    interface ParamEventPublisher<T> {
        void publish(ExchangeRequest exchangeRequest, T param);
    }
}