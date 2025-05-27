package com.boldfaced7.fxexchange.exchange.application.port.in.buy;

import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

public record CompleteKrwWithdrawalCancelCommand(
        ExchangeId exchangeId
) {
}
