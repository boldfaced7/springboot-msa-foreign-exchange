package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.ExchangeCurrencyCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.ExchangeCurrencyUseCase;
import com.boldfaced7.fxexchange.exchange.application.service.saga.ExchangeCurrencySagaOrchestrator;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;

@Primary
@UseCase
@RequiredArgsConstructor
public class ExchangeCurrencyService implements ExchangeCurrencyUseCase {

    private final ExchangeCurrencySagaOrchestrator exchangeCurrencySagaOrchestrator;

    @Override
    public ExchangeDetail exchangeCurrency(ExchangeCurrencyCommand command) {
        var toBeRequested = toModel(command);
        return exchangeCurrencySagaOrchestrator.startExchange(toBeRequested);
    }

    private ExchangeRequest toModel(ExchangeCurrencyCommand command) {
        return ExchangeRequest.of(
                command.exchangeId(),
                command.userId(),
                command.direction(),
                command.baseCurrency(),
                command.quoteCurrency(),
                command.baseAmount(),
                command.quoteAmount(),
                command.exchangeRate()
        );
    }
}
