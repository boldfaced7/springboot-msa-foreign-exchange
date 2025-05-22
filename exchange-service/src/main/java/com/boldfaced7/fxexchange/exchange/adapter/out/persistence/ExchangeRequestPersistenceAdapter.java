package com.boldfaced7.fxexchange.exchange.adapter.out.persistence;

import com.boldfaced7.fxexchange.common.PersistenceAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.out.SaveExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class ExchangeRequestPersistenceAdapter implements
        SaveExchangeRequestPort,
        UpdateExchangeRequestPort
{
        private final ExchangeRequestRepository exchangeRequestRepository;

        @Override
        public ExchangeRequest update(ExchangeRequest exchangeRequest) {
            var toBeUpdated = ExchangeRequestMapper.toJpa(exchangeRequest);
            var updated = exchangeRequestRepository.save(toBeUpdated);
            return ExchangeRequestMapper.toDomain(updated);
        }

        @Override
        public ExchangeRequest save(ExchangeRequest exchangeRequest) {
            var toBeSaved = ExchangeRequestMapper.toJpa(exchangeRequest);
            var saved = exchangeRequestRepository.save(toBeSaved);
            return ExchangeRequestMapper.toDomain(saved);
        }
}
