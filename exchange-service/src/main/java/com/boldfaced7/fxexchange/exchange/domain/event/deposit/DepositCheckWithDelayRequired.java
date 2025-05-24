package com.boldfaced7.fxexchange.exchange.domain.event.deposit;

import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public record DepositCheckWithDelayRequired(
        RequestId requestId,
        Count count
) {
    public DepositCheckWithDelayRequired(
            RequestId requestId
    ) {
        this(requestId, Count.zero());
    }
}
