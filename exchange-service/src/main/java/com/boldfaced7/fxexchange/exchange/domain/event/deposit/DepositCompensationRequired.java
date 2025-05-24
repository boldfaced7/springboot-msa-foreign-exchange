package com.boldfaced7.fxexchange.exchange.domain.event.deposit;

import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public record DepositCompensationRequired(
        RequestId requestId
) {
}
