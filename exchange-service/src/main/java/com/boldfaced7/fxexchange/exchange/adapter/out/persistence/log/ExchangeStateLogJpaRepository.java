package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.log;

import org.springframework.data.repository.Repository;

public interface ExchangeStateLogJpaRepository extends Repository<ExchangeStateLogJpa, Long> {
    ExchangeStateLogJpa save(ExchangeStateLogJpa exchangeStateLogJpa);
}