package com.boldfaced7.fxexchange.exchange.application.service.sell;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.sell.CheckKrwDepositWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.sell.CheckKrwDepositWithDelayUseCase;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class CheckKrwDepositWithDelayService implements CheckKrwDepositWithDelayUseCase {

    private final ExchangeRequestLoader exchangeRequestLoader;
    private final ExchangeEventPublisher exchangeEventPublisher;

    @Override
    public void checkKrwDepositWithDelay(CheckKrwDepositWithDelayCommand command) {
        ExchangeRequest requested = exchangeRequestLoader.loadExchangeRequest(command.requestId());
        requested.checkingKrwDepositRequired(command.count());
        exchangeEventPublisher.publishEvents(requested);
    }
}
