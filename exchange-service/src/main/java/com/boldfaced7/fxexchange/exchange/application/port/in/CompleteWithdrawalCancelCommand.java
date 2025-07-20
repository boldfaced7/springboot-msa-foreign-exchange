package com.boldfaced7.fxexchange.exchange.application.port.in;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.cancel.WithdrawalCancelId;

public record CompleteWithdrawalCancelCommand(
        WithdrawalCancelId withdrawalCancelId,
        ExchangeId exchangeId,
        Direction direction
) {
}
