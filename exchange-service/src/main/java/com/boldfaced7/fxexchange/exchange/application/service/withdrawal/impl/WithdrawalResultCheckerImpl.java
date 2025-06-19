package com.boldfaced7.fxexchange.exchange.application.service.withdrawal.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.LoadWithdrawalResultPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.ParamEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.SimpleEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.withdrawal.WithdrawalResultChecker;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WithdrawalResultCheckerImpl implements WithdrawalResultChecker {

    private final LoadWithdrawalResultPort loadWithdrawalResultPort;
    private final ExchangeEventPublisher exchangeEventPublisher;

    public WithdrawalResult loadWithdrawalResult(
            ExchangeRequest requested,
            SimpleEventPublisher whenSucceed,
            SimpleEventPublisher whenFailed,
            ParamEventPublisher<Count> whenExceptionOccurred,
            Count count
    ) {
        try {
            var deposited = loadWithdrawalResultPort.loadWithdrawalResult(
                    requested.getExchangeId(),
                    requested.getDirection()
            );
            var publisher = (deposited.isSuccess()) ? whenSucceed : whenFailed;
            exchangeEventPublisher.publishEvents(requested, publisher);

            return deposited;

        } catch (Exception e) {
            exchangeEventPublisher.publishEvents(
                    requested,
                    whenExceptionOccurred,
                    count
            );
            throw e;
        }
    }

}
