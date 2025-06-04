package com.boldfaced7.fxexchange.exchange.application.port.out;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

import java.time.Duration;

public interface SendWithdrawalCheckRequestPort {
    void sendWithdrawalCheckRequest(ExchangeId exchangeId, Duration delay);
    Direction direction();
}
