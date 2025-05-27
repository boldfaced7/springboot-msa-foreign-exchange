package com.boldfaced7.fxexchange.exchange.application.service.sell;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.sell.CompleteFxWithdrawalCancelCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.sell.CompleteFxWithdrawalCancelUseCase;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class CompleteFxWithdrawalCancelService implements CompleteFxWithdrawalCancelUseCase {

    private final ExchangeRequestLoader exchangeRequestLoader;
    private final ExchangeEventPublisher exchangeEventPublisher;

    @Override
    public void completeFxWithdrawalCompensation(CompleteFxWithdrawalCancelCommand command) {
        var requested = exchangeRequestLoader.loadExchangeRequest(command.exchangeId());
        requested.sellingFailed();
        exchangeEventPublisher.publishEvents(requested);
    }
}
