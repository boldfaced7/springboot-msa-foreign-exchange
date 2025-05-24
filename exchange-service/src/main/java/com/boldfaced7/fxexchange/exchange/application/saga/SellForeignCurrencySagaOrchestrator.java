package com.boldfaced7.fxexchange.exchange.application.saga;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeDetail;

public interface SellForeignCurrencySagaOrchestrator {
    ExchangeDetail startExchange(ExchangeRequest requested);
}
