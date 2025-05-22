package com.boldfaced7.fxexchange.exchange.application.port.in;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;

public interface SellForeignCurrencyUseCase {
    ExchangeRequest sell(SellForeignCurrencyCommand command);
}
