package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.exchange;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Optional;

public interface ExchangeRequestJpaRepository extends JpaRepository<ExchangeRequestJpa, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM ExchangeRequestJpa e WHERE e.exchangeRequestId = :requestId")
    @QueryHints(
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "1000")
    )
    Optional<ExchangeRequestJpa> findByRequestIdForUpdate(Long requestId);
}
