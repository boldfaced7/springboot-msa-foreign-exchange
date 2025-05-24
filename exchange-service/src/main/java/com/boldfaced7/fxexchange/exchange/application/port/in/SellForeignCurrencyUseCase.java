package com.boldfaced7.fxexchange.exchange.application.port.in;

import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeDetail;

public interface SellForeignCurrencyUseCase {
    ExchangeDetail sellForeignCurrency(SellForeignCurrencyCommand command);
}
