package com.boldfaced7.fxexchange.exchange.adapter.out.external.warning;

import com.boldfaced7.fxexchange.common.ExternalSystemAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.out.SendWarningMessagePort;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@ExternalSystemAdapter
@RequiredArgsConstructor
public class NoOpWarningMessageSender implements SendWarningMessagePort {

    @Override
    public void sendWarningMessage(RequestId requestId, ExchangeId exchangeId) {

    }
}
