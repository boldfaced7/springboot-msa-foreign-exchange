package com.boldfaced7.fxexchange.exchange.domain.model;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.enums.ExchangeState;
import com.boldfaced7.fxexchange.exchange.domain.vo.log.LogId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeStateLog {
    private LogId logId;
    private RequestId requestId;
    private Direction direction;
    private ExchangeState state;
    private LocalDateTime raisedAt;

    public static ExchangeStateLog of(
            RequestId requestId,
            Direction direction,
            ExchangeState state,
            LocalDateTime raisedAt
    ) {
        return new ExchangeStateLog(
                null,
                requestId,
                direction,
                state,
                raisedAt
        );
    }

    public static ExchangeStateLog of(
            LogId logId,
            RequestId requestId,
            Direction direction,
            ExchangeState state,
            LocalDateTime raisedAt
    ) {
    return new ExchangeStateLog(
            logId,
            requestId,
            direction,
            state,
            raisedAt
    );
}

}
