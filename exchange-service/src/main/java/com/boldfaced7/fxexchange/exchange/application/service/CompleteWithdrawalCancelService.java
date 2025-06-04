package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.CompleteWithdrawalCancelCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.CompleteWithdrawalCancelUseCase;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class CompleteWithdrawalCancelService implements CompleteWithdrawalCancelUseCase {

    private final ExchangeEventPublisher exchangeEventPublisher;
    private final ExchangeRequestUpdater exchangeRequestUpdater;

    @Override
    @Transactional
    public void completeWithdrawalCancel(CompleteWithdrawalCancelCommand command) {
        exchangeRequestUpdater.update(
                command.exchangeId(),
                ExchangeRequest::addWithdrawalCancelId,
                command.withdrawalCancelId()
        );
        exchangeEventPublisher.publishEvents(
                command.exchangeId(),
                ExchangeRequest::withdrawalCancelled
        );
    }
}
