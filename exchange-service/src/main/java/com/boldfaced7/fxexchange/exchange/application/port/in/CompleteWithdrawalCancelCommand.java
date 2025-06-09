package com.boldfaced7.fxexchange.exchange.application.port.in;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalCancelId;

public record CompleteWithdrawalCancelCommand(
        ExchangeId exchangeId,
        WithdrawalCancelId withdrawalCancelId,
        Direction direction
) {
}
