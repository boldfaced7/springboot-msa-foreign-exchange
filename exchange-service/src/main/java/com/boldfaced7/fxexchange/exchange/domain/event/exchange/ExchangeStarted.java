package com.boldfaced7.fxexchange.exchange.domain.event.exchange;

import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public record ExchangeStarted(
        RequestId requestId
) {
}
