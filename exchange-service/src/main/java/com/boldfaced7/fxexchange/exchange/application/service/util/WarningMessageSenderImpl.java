package com.boldfaced7.fxexchange.exchange.application.service.util;

import com.boldfaced7.fxexchange.exchange.application.port.out.SendWarningMessagePort;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarningMessageSenderImpl implements WarningMessageSender {

    private final SendWarningMessagePort sendWarningMessagePort;

    @Override
    public void sendWarningMessage(RequestId requestId, ExchangeId exchangeId) {
        sendWarningMessagePort.sendWarningMessage(requestId, exchangeId);
    }
}
