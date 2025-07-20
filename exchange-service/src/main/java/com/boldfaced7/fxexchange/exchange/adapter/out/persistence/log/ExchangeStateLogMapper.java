package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.log;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeStateLog;
import com.boldfaced7.fxexchange.exchange.domain.vo.log.LogId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;

public class ExchangeStateLogMapper {
    public static JpaExchangeStateLog toJpa(ExchangeStateLog exchangeStateLog) {
        return new JpaExchangeStateLog(
                (exchangeStateLog.getLogId() == null) ? null : exchangeStateLog.getLogId().value(),
                exchangeStateLog.getRequestId().value(),
                exchangeStateLog.getDirection(),
                exchangeStateLog.getState(),
                exchangeStateLog.getRaisedAt()
        );
    }

    public static ExchangeStateLog toDomain(JpaExchangeStateLog jpaExchangeStateLog) {
        return ExchangeStateLog.of(
                (jpaExchangeStateLog.getLogId() == null) ? null : new LogId(jpaExchangeStateLog.getLogId()),
                new RequestId(jpaExchangeStateLog.getRequestId()),
                jpaExchangeStateLog.getDirection(),
                jpaExchangeStateLog.getState(),
                jpaExchangeStateLog.getRaisedAt()
        );
    }
}
