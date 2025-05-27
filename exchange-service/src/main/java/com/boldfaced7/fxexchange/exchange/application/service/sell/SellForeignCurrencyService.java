package com.boldfaced7.fxexchange.exchange.application.service.sell;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.sell.SellForeignCurrencyCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.sell.SellForeignCurrencyUseCase;
import com.boldfaced7.fxexchange.exchange.application.port.out.SaveExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.saga.SellForeignCurrencySagaOrchestrator;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeDetail;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class SellForeignCurrencyService implements SellForeignCurrencyUseCase {

    private final SaveExchangeRequestPort saveExchangeRequestPort;
    private final ExchangeEventPublisher exchangeEventPublisher;
    private final SellForeignCurrencySagaOrchestrator sellForeignCurrencySagaOrchestrator;

    @Override
    public ExchangeDetail sellForeignCurrency(SellForeignCurrencyCommand command) {
        var saved = saveExchangeRequestPort.save(toModel(command));

        saved.sellingStarted();
        exchangeEventPublisher.publishEvents(saved);

        return sellForeignCurrencySagaOrchestrator.startExchange(saved);
    }

    private ExchangeRequest toModel(SellForeignCurrencyCommand command) {
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
