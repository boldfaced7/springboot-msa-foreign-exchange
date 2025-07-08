package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.exchange;

import com.boldfaced7.fxexchange.common.PersistenceAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.out.exchange.LoadExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.exchange.SaveExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.exchange.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class ExchangeRequestPersistenceAdapter implements
        LoadExchangeRequestPort,
        SaveExchangeRequestPort,
        UpdateExchangeRequestPort
{
    private final ExchangeRequestJpaRepository exchangeRequestJpaRepository;

    @Override
    public Optional<ExchangeRequest> loadByRequestIdForUpdate(RequestId requestId) {
        return exchangeRequestJpaRepository.findByRequestIdForUpdate(
                requestId.value()
        ).map(ExchangeRequestMapper::toDomain);
    }

    @Override
    public ExchangeRequest save(ExchangeRequest exchangeRequest) {
        return persist(exchangeRequest);
    }

    @Override
    public ExchangeRequest update(ExchangeRequest exchangeRequest) {
        return persist(exchangeRequest);
    }

    private ExchangeRequest persist(ExchangeRequest exchangeRequest) {
        var toBePersisted = ExchangeRequestMapper.toJpa(exchangeRequest);
        var persisted = exchangeRequestJpaRepository.save(toBePersisted);
        return ExchangeRequestMapper.toDomain(persisted);
    }
}
