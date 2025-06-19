package com.boldfaced7.fxexchange.exchange.application.service.deposit.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.RequestDepositPort;
import com.boldfaced7.fxexchange.exchange.application.service.deposit.DepositRequester;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.ParamEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.SimpleEventPublisher;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositRequesterImpl implements DepositRequester {

    private final RequestDepositPort requestDepositPort;
    private final ExchangeEventPublisher exchangeEventPublisher;

    public DepositResult requestDeposit(
            ExchangeRequest requested,
            SimpleEventPublisher whenSucceed,
            SimpleEventPublisher whenFailed,
            ParamEventPublisher<Count> whenExceptionOccurred
    ) {
        try {
            var deposited = requestDepositPort.deposit(requested);
            var publisher = (deposited.isSuccess()) ? whenSucceed : whenFailed;
            exchangeEventPublisher.publishEvents(requested, publisher);
            return deposited;
        } catch (Exception e) {
            exchangeEventPublisher.publishEvents(requested, whenExceptionOccurred, Count.zero());
            throw e;
        }
    }
}
