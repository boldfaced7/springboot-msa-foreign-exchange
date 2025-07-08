package com.boldfaced7.fxexchange.exchange.application.port.in;

import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import java.time.LocalDateTime;

public record LogExchangeStateCommand(
        RequestId requestId,
        Direction direction,
        LocalDateTime raisedAt
) {
}
