package com.boldfaced7.fxexchange.exchange.domain.event.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public record WithdrawalCheckWithDelayRequired(
        RequestId requestId,
        Count count
) {
    public WithdrawalCheckWithDelayRequired(
            RequestId requestId
    ) {
        this(requestId, Count.zero());
    }

}
