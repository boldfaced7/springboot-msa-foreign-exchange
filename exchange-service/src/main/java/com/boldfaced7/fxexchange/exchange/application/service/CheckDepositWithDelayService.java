package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckDepositWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckDepositWithDelayUseCase;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import lombok.RequiredArgsConstructor;
@UseCase
@RequiredArgsConstructor
public class CheckDepositWithDelayService implements CheckDepositWithDelayUseCase {

    private final ExchangeEventPublisher exchangeEventPublisher;

    @Override
    public void checkDepositWithDelay(CheckDepositWithDelayCommand command) {
        exchangeEventPublisher.publishEvents(
                command.exchangeId(),
                ExchangeRequest::depositResultUnknown,
                command.count()
        );
    }
}
