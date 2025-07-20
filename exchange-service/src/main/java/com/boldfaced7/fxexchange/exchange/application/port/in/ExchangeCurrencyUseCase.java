package com.boldfaced7.fxexchange.exchange.application.port.in;

import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeDetail;

public interface ExchangeCurrencyUseCase {
    ExchangeDetail exchangeCurrency(ExchangeCurrencyCommand command);
}
