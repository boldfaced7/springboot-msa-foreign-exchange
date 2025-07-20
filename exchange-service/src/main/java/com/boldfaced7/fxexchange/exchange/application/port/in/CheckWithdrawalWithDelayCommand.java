package com.boldfaced7.fxexchange.exchange.application.port.in;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;

public record CheckWithdrawalWithDelayCommand(
        ExchangeId exchangeId,
        Count count,
        Direction direction
) {
}
