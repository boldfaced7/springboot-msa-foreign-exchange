package com.boldfaced7.fxexchange.exchange.application.service.util;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public interface ExchangeRequestUpdater {
    ExchangeRequest update(ExchangeRequest exchangeRequest, SimpleRequestUpdater simpleRequestUpdater);
    ExchangeRequest update(RequestId requestId, SimpleRequestUpdater simpleRequestUpdater);
    ExchangeRequest update(ExchangeId exchangeId, SimpleRequestUpdater simpleRequestUpdater);

    <T> ExchangeRequest update(ExchangeRequest exchangeRequest, ParamRequestUpdater<T> paramRequestUpdater, T param);
    <T> ExchangeRequest update(RequestId requestId, ParamRequestUpdater<T> paramRequestUpdater, T param);
    <T> ExchangeRequest update(ExchangeId exchangeId, ParamRequestUpdater<T> paramRequestUpdater, T param);

    @FunctionalInterface
    interface SimpleRequestUpdater {
        void update(ExchangeRequest exchangeRequest);
    }

    @FunctionalInterface
    interface ParamRequestUpdater<T> {
        void update(ExchangeRequest exchangeRequest, T param);
    }

}
