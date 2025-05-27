package com.boldfaced7.fxexchange.exchange.application.port.in.sell;

import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

public record CompleteFxWithdrawalCancelCommand(
        ExchangeId exchangeId
) {
}
