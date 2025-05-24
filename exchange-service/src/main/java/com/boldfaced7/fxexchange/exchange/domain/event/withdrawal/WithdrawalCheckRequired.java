package com.boldfaced7.fxexchange.exchange.domain.event.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public record WithdrawalCheckRequired(
        RequestId requestId
) {
}
