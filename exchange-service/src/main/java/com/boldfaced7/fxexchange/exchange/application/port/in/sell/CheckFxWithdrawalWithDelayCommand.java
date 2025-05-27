package com.boldfaced7.fxexchange.exchange.application.port.in.sell;

import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public record CheckFxWithdrawalWithDelayCommand(
        RequestId requestId,
        Count count
) {
}
