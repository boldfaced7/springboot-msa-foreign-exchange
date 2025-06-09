package com.boldfaced7.fxexchange.exchange.application.port.out;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

import java.time.Duration;

public interface SendDepositCheckRequestPort {
    void sendDepositCheckRequest(ExchangeId exchangeId, Duration delay, Count count, Direction direction);
}
