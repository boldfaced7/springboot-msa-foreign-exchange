package com.boldfaced7.fxexchange.exchange.application.port.out.external;

import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;

public interface SendWarningMessagePort {
    void sendWarningMessage(RequestId requestId, ExchangeId exchangeId);
}
