package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.ExchangeCurrencyCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.ExchangeCurrencyUseCase;
import com.boldfaced7.fxexchange.exchange.application.port.out.SaveExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.saga.ExchangeCurrencySagaOrchestrator;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeDetail;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class ExchangeCurrencyService implements ExchangeCurrencyUseCase {

    private final SaveExchangeRequestPort saveExchangeRequestPort;
    private final ExchangeEventPublisher exchangeEventPublisher;
    private final ExchangeCurrencySagaOrchestrator exchangeCurrencySagaOrchestrator;

    @Override
    public ExchangeDetail exchangeCurrency(ExchangeCurrencyCommand command) {
        ExchangeRequest saved = saveExchangeRequestPort.save(toModel(command));
        exchangeEventPublisher.publishEvents(
                command.exchangeId(),
                ExchangeRequest::exchangeCurrencyStarted
        );
        return exchangeCurrencySagaOrchestrator.startExchange(saved);
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
