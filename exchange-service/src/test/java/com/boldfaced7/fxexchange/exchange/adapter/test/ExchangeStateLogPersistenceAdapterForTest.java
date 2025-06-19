package com.boldfaced7.fxexchange.exchange.adapter.test;

import com.boldfaced7.fxexchange.exchange.application.port.out.SaveExchangeStateLogPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeStateLog;
import com.boldfaced7.fxexchange.exchange.domain.vo.LogId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@TestConfiguration
@Profile("application-test")
public class ExchangeStateLogPersistenceAdapterForTest implements
        SaveExchangeStateLogPort
{
    private final AtomicLong id = new AtomicLong(1);
    private final ConcurrentHashMap<LogId, ExchangeStateLog> exchangeStateLogs = new ConcurrentHashMap<>();
    
    @Override
    public ExchangeStateLog save(ExchangeStateLog exchangeStateLog) {
        log.info("state to be logged: {}", exchangeStateLog.getState());

        LogId logId = new LogId(id.getAndIncrement());

        var toBeSaved = ExchangeStateLog.of(
            logId,
            exchangeStateLog.getRequestId(),
            exchangeStateLog.getDirection(),
            exchangeStateLog.getState(),
            exchangeStateLog.getRaisedAt()
        );
        exchangeStateLogs.put(logId, toBeSaved);
        log.info("state logged: {}", exchangeStateLogs.get(logId).getState());
        return toBeSaved;
    }

    public ExchangeStateLog findByLogId(LogId logId) {
        return exchangeStateLogs.get(logId);
    }
    
    public List<ExchangeStateLog> findByRequestId(RequestId requestId) {
        return exchangeStateLogs.values().stream()
                .filter(exchangeStateLog -> exchangeStateLog.getRequestId().equals(requestId))
                .toList();
    }

    public void reset() {
        exchangeStateLogs.clear();
        id.set(1);
    }
}
