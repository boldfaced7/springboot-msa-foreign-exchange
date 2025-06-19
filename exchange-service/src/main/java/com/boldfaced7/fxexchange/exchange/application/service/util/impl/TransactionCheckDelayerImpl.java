package com.boldfaced7.fxexchange.exchange.application.service.util.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.ScheduleCheckRequestPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.TransactionCheckDelayer;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.enums.TransactionCheckType;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionCheckDelayerImpl implements TransactionCheckDelayer {

    private final static Duration DELAY_SECOND = Duration.ofSeconds(30);

    private final ScheduleCheckRequestPort scheduleCheckRequestPort;

    @Override
    public void delayTransactionCheck(
            ExchangeId exchangeId,
            Count count,
            Direction direction,
            TransactionCheckType transactionCheckType
    ) {
        Duration delay = DELAY_SECOND.multipliedBy(count.value() + 1);
        scheduleCheckRequestPort.scheduleCheckRequest(
                exchangeId,
                delay,
                count,
                direction,
                transactionCheckType
        );
    }
}
