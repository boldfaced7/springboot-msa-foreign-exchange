package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.request;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRequestJpaRepository extends JpaRepository<ExchangeRequestJpa, Long> {
    ExchangeRequestJpa findByExchangeRequestId(Long exchangeRequestId);
    ExchangeRequestJpa findByExchangeId(String exchangeId);
}
