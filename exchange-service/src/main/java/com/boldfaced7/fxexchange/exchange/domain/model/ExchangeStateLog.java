package com.boldfaced7.fxexchange.exchange.domain.model;

import com.boldfaced7.fxexchange.exchange.domain.enums.ExchangeState;
import com.boldfaced7.fxexchange.exchange.domain.vo.LogId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeStateLog {
    private LogId logId;
    private RequestId requestId;
    private ExchangeState state;
    private LocalDateTime raisedAt;

    public static ExchangeStateLog of(
            RequestId requestId,
            ExchangeState state,
            LocalDateTime raisedAt
    ) {
        return new ExchangeStateLog(
                null,
                requestId,
                state,
                raisedAt
        );
    }

    public static ExchangeStateLog of(
            LogId logId,
            RequestId requestId,
            ExchangeState state,
            LocalDateTime raisedAt
    ) {
    return new ExchangeStateLog(
            logId,
            requestId,
            state,
            raisedAt
    );
}

}
