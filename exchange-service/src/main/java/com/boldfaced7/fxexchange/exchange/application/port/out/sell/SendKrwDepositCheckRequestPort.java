package com.boldfaced7.fxexchange.exchange.application.port.out.sell;

import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.Duration;

public interface SendKrwDepositCheckRequestPort {
    void sendKrwDepositCheckRequest(RequestId requestId, Duration delay);
}
