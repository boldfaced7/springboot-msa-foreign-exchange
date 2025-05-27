package com.boldfaced7.fxexchange.exchange.application.service.buy;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.buy.CheckFxDepositWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.buy.CheckFxDepositWithDelayUseCase;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class CheckFxDepositWithDelayService implements CheckFxDepositWithDelayUseCase {

    private final ExchangeRequestLoader exchangeRequestLoader;
    private final ExchangeEventPublisher exchangeEventPublisher;

    @Override
    public void checkFxDepositWithDelay(CheckFxDepositWithDelayCommand command) {
        var requested = exchangeRequestLoader.loadExchangeRequest(command.requestId());
        requested.checkingFxDepositRequired(command.count());
        exchangeEventPublisher.publishEvents(requested);
    }
}
