package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.log;

import com.boldfaced7.fxexchange.exchange.application.port.out.log.SaveExchangeStateLogPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeStateLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangeStateLogPersistenceAdapter implements SaveExchangeStateLogPort {

    private final JpaExchangeStateLogRepository jpaExchangeStateLogRepository;


    @Override
    public ExchangeStateLog save(ExchangeStateLog exchangeStateLog) {
        JpaExchangeStateLog toBeSaved = ExchangeStateLogMapper.toJpa(exchangeStateLog);
        JpaExchangeStateLog savedJpa = jpaExchangeStateLogRepository.save(toBeSaved);
        return ExchangeStateLogMapper.toDomain(savedJpa);
    }
}
