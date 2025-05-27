package com.boldfaced7.fxexchange.exchange.application.service.buy;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.buy.CompleteKrwWithdrawalCancelCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.buy.CompleteKrwWithdrawalCancelUseCase;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class CompleteKrwWithdrawalCancelService implements CompleteKrwWithdrawalCancelUseCase {

    private final ExchangeRequestLoader exchangeRequestLoader;
    private final ExchangeEventPublisher exchangeEventPublisher;

    @Override
    public void completeKrwWithdrawalCancel(CompleteKrwWithdrawalCancelCommand command) {
        var requested = exchangeRequestLoader.loadExchangeRequest(command.exchangeId());
        requested.buyingFailed();
        exchangeEventPublisher.publishEvents(requested);
    }
}
