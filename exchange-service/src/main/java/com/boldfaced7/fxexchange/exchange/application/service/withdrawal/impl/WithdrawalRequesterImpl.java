package com.boldfaced7.fxexchange.exchange.application.service.withdrawal.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.RequestWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.ParamEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.SimpleEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.withdrawal.WithdrawalRequester;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WithdrawalRequesterImpl implements WithdrawalRequester {

    private final RequestWithdrawalPort requestWithdrawalPort;
    private final ExchangeEventPublisher exchangeEventPublisher;

    public WithdrawalResult requestWithdrawal(
            ExchangeRequest requested,
            SimpleEventPublisher whenSucceed,
            SimpleEventPublisher whenFailed,
            ParamEventPublisher<Count> whenExceptionOccurred
    ) {
        try {
            var withdrawn = requestWithdrawalPort.withdraw(requested);
            var publisher = (withdrawn.isSuccess()) ? whenSucceed : whenFailed;
            exchangeEventPublisher.publishEvents(requested, publisher);
            return withdrawn;
        } catch (Exception e) {
            exchangeEventPublisher.publishEvents(requested, whenExceptionOccurred, Count.zero());
            throw e;
        }
    }
}
