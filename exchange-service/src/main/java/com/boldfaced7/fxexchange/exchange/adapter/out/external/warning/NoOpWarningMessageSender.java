package com.boldfaced7.fxexchange.exchange.adapter.out.external.warning;

import com.boldfaced7.fxexchange.common.ExternalSystemAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.out.external.SendWarningMessagePort;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;
import lombok.RequiredArgsConstructor;

@ExternalSystemAdapter
@RequiredArgsConstructor
public class NoOpWarningMessageSender implements SendWarningMessagePort {

    @Override
    public void sendWarningMessage(RequestId requestId, ExchangeId exchangeId) {

    }
}
