package com.boldfaced7.fxexchange.exchange.application.service.sell;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.sell.CheckFxWithdrawalWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.sell.CheckFxWithdrawalWithDelayUseCase;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class CheckFxWithdrawalWithDelayService implements CheckFxWithdrawalWithDelayUseCase {

    private final ExchangeRequestLoader exchangeRequestLoader;
    private final ExchangeEventPublisher exchangeEventPublisher;

    @Override
    public void checkFxWithdrawalWithDelay(CheckFxWithdrawalWithDelayCommand command) {
        ExchangeRequest requested = exchangeRequestLoader.loadExchangeRequest(command.requestId());
        requested.checkingFxWithdrawalRequired(command.count());
        exchangeEventPublisher.publishEvents(requested);
    }
}
