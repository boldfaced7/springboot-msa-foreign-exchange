package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.exchange;

import com.boldfaced7.fxexchange.common.PersistenceAdapter;
import com.boldfaced7.fxexchange.exchange.application.exception.ExchangeAlreadyRequestedException;
import com.boldfaced7.fxexchange.exchange.application.port.out.exchange.LoadExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.exchange.SaveExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.exchange.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class ExchangeRequestPersistenceAdapter implements
        LoadExchangeRequestPort,
        SaveExchangeRequestPort,
        UpdateExchangeRequestPort
{
    private final JpaExchangeRequestRepository jpaExchangeRequestRepository;

    @Override
    public Optional<ExchangeRequest> loadByRequestIdForUpdate(RequestId requestId) {
        return jpaExchangeRequestRepository.findByRequestIdForUpdate(requestId.value())
                .map(ExchangeRequestMapper::toDomain);
    }

    @Override
    public ExchangeRequest save(ExchangeRequest exchange) {
        try {
            return persist(exchange);
        } catch (DataIntegrityViolationException e) {
            throw new ExchangeAlreadyRequestedException();
        }
    }

    @Override
    public ExchangeRequest update(ExchangeRequest exchange) {
        return persist(exchange);
    }

    private ExchangeRequest persist(ExchangeRequest exchange) {
        var toBePersisted = ExchangeRequestMapper.toJpa(exchange);
        var persisted = jpaExchangeRequestRepository.save(toBePersisted);
        return ExchangeRequestMapper.toDomain(persisted);
    }

}
