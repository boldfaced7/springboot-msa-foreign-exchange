package com.boldfaced7.fxexchange.exchange.application.service.buy;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.buy.BuyForeignCurrencyCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.buy.BuyForeignCurrencyUseCase;
import com.boldfaced7.fxexchange.exchange.application.port.out.SaveExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.saga.BuyForeignCurrencySagaOrchestrator;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeDetail;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class BuyForeignCurrencyService implements BuyForeignCurrencyUseCase {

    private final SaveExchangeRequestPort saveExchangeRequestPort;
    private final ExchangeEventPublisher exchangeEventPublisher;
    private final BuyForeignCurrencySagaOrchestrator buyForeignCurrencySagaOrchestrator;

    @Override
    public ExchangeDetail buyForeignCurrency(BuyForeignCurrencyCommand command) {
        var saved = saveExchangeRequestPort.save(toModel(command));
        saved.buyingStarted();
        exchangeEventPublisher.publishEvents(saved);

        return buyForeignCurrencySagaOrchestrator.startExchange(saved);
    }

    private ExchangeRequest toModel(BuyForeignCurrencyCommand command) {
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
