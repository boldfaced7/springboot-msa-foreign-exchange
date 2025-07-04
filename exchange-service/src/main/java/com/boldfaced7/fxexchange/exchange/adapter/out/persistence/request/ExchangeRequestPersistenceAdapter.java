package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.request;

import com.boldfaced7.fxexchange.common.PersistenceAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.out.LoadExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.SaveExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class ExchangeRequestPersistenceAdapter implements
        LoadExchangeRequestPort,
        SaveExchangeRequestPort,
        UpdateExchangeRequestPort
{
    private final ExchangeRequestJpaRepository exchangeRequestJpaRepository;

    @Override
    public ExchangeRequest loadByRequestId(RequestId requestId) {
        var found = exchangeRequestJpaRepository.findByExchangeRequestId(
                requestId.value()
        );
        if (found == null) {
            throw new IllegalStateException("환전 요청을 찾을 수 없습니다: " + requestId.value());
        }
        return ExchangeRequestMapper.toDomain(found);
    }

    @Override
    public ExchangeRequest loadByExchangeId(ExchangeId exchangeId) {
        var found = exchangeRequestJpaRepository.findByExchangeId(
                exchangeId.value()
        );
        if (found == null) {
            throw new IllegalStateException("환전 요청을 찾을 수 없습니다: " + exchangeId.value());
        }
        return ExchangeRequestMapper.toDomain(found);
    }

    @Override
    public ExchangeRequest save(ExchangeRequest exchangeRequest) {
        ExchangeRequestJpa toBeSaved = ExchangeRequestMapper.toJpa(exchangeRequest);
        ExchangeRequestJpa saved = exchangeRequestJpaRepository.save(toBeSaved);
        return ExchangeRequestMapper.toDomain(saved);
    }

    @Override
    public ExchangeRequest update(ExchangeRequest exchangeRequest) {
        var toBeUpdated = ExchangeRequestMapper.toJpa(exchangeRequest);
        var updated = exchangeRequestJpaRepository.save(toBeUpdated);
        return ExchangeRequestMapper.toDomain(updated);
    }
}
