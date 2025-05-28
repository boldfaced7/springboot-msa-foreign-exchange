package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.log;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeStateLog;
import com.boldfaced7.fxexchange.exchange.domain.vo.LogId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public class ExchangeStateLogMapper {
    public static ExchangeStateLogJpa toJpa(ExchangeStateLog exchangeStateLog) {
        return new ExchangeStateLogJpa(
                exchangeStateLog.getLogId().value(),
                exchangeStateLog.getRequestId().value(),
                exchangeStateLog.getState(),
                exchangeStateLog.getRaisedAt()
        );
    }

    public static ExchangeStateLog toDomain(ExchangeStateLogJpa exchangeStateLogJpa) {
        return ExchangeStateLog.of(
                new LogId(exchangeStateLogJpa.getLogId()),
                new RequestId(exchangeStateLogJpa.getRequestId()),
                exchangeStateLogJpa.getState(),
                exchangeStateLogJpa.getRaisedAt()
        );
    }
}
