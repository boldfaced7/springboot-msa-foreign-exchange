package com.boldfaced7.fxexchange.exchange.adapter.out.persistence;

import org.springframework.data.repository.Repository;

public interface ExchangeRequestRepository extends Repository<ExchangeRequestJpa, Long> {
    ExchangeRequestJpa save(ExchangeRequestJpa exchangeRequestJpa);
}
