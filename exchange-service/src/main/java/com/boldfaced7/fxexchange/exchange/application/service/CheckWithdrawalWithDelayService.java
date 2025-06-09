package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckWithdrawalWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckWithdrawalWithDelayUseCase;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class CheckWithdrawalWithDelayService implements CheckWithdrawalWithDelayUseCase {

    private final ExchangeEventPublisher exchangeEventFacilitator;


    @Override
    public void checkWithdrawalWithDelay(CheckWithdrawalWithDelayCommand command) {
        exchangeEventFacilitator.publishEvents(
                command.exchangeId(),
                ExchangeRequest::withdrawalResultUnknown,
                command.count()
        );
    }
}
