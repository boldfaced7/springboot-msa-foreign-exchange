package com.boldfaced7.fxexchange.exchange.application.port.out;

import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public interface SendWarningMessagePort {
    void sendWarningMessage(RequestId requestId, ExchangeId exchangeId);
}
