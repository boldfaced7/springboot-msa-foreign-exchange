package com.boldfaced7.fxexchange.exchange.application.service.buy;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.buy.CheckKrwWithdrawalWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.buy.CheckKrwWithdrawalWithDelayUseCase;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class CheckKrwWithdrawalWithDelayService implements CheckKrwWithdrawalWithDelayUseCase {

    private final ExchangeRequestLoader exchangeRequestLoader;
    private final ExchangeEventPublisher exchangeEventPublisher;

    @Override
    public void checkKrwWithdrawalWithDelay(CheckKrwWithdrawalWithDelayCommand command) {
        var requested = exchangeRequestLoader.loadExchangeRequest(command.requestId());
        requested.checkingKrwWithdrawalRequired(command.count());
        exchangeEventPublisher.publishEvents(requested);
    }
}
