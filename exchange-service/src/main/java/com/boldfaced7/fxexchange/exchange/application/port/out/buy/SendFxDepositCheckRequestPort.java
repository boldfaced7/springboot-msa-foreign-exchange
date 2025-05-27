package com.boldfaced7.fxexchange.exchange.application.port.out.buy;

import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.Duration;

public interface SendFxDepositCheckRequestPort {
    void sendFxDepositCheckRequest(RequestId requestId, Duration delay);
}
