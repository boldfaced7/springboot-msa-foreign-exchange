package com.boldfaced7.fxexchange.exchange.application.service.util;

import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public interface WarningMessageSender {
    void sendWarningMessage(RequestId requestId, ExchangeId exchangeId);
}
