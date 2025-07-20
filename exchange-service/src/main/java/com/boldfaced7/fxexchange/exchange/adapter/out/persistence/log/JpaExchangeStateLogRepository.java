package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.log;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaExchangeStateLogRepository extends JpaRepository<JpaExchangeStateLog, Long> {}