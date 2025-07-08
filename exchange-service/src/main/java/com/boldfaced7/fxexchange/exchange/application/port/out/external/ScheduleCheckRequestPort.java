package com.boldfaced7.fxexchange.exchange.application.port.out.external;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.enums.TransactionType;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

import java.time.Duration;

public interface ScheduleCheckRequestPort {
    void scheduleCheckRequest(
            ExchangeId exchangeId,
            Duration delay,
            Count count,
            Direction direction,
            TransactionType transactionType
    );
}
