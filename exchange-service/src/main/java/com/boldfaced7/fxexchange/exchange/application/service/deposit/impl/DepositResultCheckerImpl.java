package com.boldfaced7.fxexchange.exchange.application.service.deposit.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.LoadDepositResultPort;
import com.boldfaced7.fxexchange.exchange.application.service.deposit.DepositResultChecker;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.ParamEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.SimpleEventPublisher;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DepositResultCheckerImpl implements DepositResultChecker {

    private final Map<Direction, LoadDepositResultPort> loadDepositResultPorts;
    private final ExchangeEventPublisher exchangeEventPublisher;

    public DepositResult loadDepositResult(
            ExchangeRequest requested,
            SimpleEventPublisher whenSucceed,
            SimpleEventPublisher whenFailed,
            ParamEventPublisher<Count> whenExceptionOccurred,
            Count count
    ) {
        try {
            var deposited = loadDepositResultPorts.get(requested.getDirection())
                    .loadDepositResult(requested.getExchangeId());

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
