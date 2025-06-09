package com.boldfaced7.fxexchange.exchange.adapter.test;

import com.boldfaced7.fxexchange.exchange.application.port.out.SendWarningMessagePort;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import org.springframework.context.annotation.Profile;

@Profile("test")
public class NoOpSendWarningMessagePort implements SendWarningMessagePort {


    @Override
    public void sendWarningMessage(RequestId requestId, ExchangeId exchangeId) {
    }
}
