package com.boldfaced7.fxexchange.exchange.application.port.in.buy;

import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public record CheckKrwWithdrawalWithDelayCommand(
        RequestId requestId,
        Count count
) {
}
