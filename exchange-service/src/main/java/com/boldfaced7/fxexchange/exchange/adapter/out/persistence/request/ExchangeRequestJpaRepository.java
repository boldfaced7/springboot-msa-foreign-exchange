package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.request;

import org.springframework.data.repository.Repository;

public interface ExchangeRequestJpaRepository extends Repository<ExchangeRequestJpa, Long> {
    ExchangeRequestJpa findByExchangeRequestId(Long exchangeRequestId);
    ExchangeRequestJpa findByExchangeId(String exchangeId);
    ExchangeRequestJpa save(ExchangeRequestJpa exchangeRequestJpa);
}
