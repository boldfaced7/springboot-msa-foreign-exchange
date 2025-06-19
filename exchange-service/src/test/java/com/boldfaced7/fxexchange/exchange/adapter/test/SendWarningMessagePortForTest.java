package com.boldfaced7.fxexchange.exchange.adapter.test;

import com.boldfaced7.fxexchange.exchange.application.port.out.SendWarningMessagePort;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("application-test")
public class SendWarningMessagePortForTest implements SendWarningMessagePort {


    @Override
    public void sendWarningMessage(RequestId requestId, ExchangeId exchangeId) {
    }
}
