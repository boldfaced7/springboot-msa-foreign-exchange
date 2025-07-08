package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.log;

import com.boldfaced7.fxexchange.exchange.application.port.out.log.SaveExchangeStateLogPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeStateLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangeStateLogPersistenceAdapter implements SaveExchangeStateLogPort {

    private final ExchangeStateLogJpaRepository exchangeStateLogJpaRepository;


    @Override
    public ExchangeStateLog save(ExchangeStateLog exchangeStateLog) {
        ExchangeStateLogJpa toBeSaved = ExchangeStateLogMapper.toJpa(exchangeStateLog);
        ExchangeStateLogJpa savedJpa = exchangeStateLogJpaRepository.save(toBeSaved);
        return ExchangeStateLogMapper.toDomain(savedJpa);
    }
}
