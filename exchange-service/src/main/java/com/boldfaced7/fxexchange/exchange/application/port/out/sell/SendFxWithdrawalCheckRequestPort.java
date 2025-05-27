package com.boldfaced7.fxexchange.exchange.application.port.out.sell;

import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.Duration;

public interface SendFxWithdrawalCheckRequestPort {
    void sendFxWithdrawalCheckRequest(RequestId requestId, Duration delay);
}
