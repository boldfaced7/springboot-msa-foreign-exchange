package com.boldfaced7.fxexchange.exchange.adapter.out.external;

import com.boldfaced7.fxexchange.common.ExternalSystemAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.out.DepositKrwPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositId;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@ExternalSystemAdapter
@RequiredArgsConstructor
public class NoOpDepositKrwAdapter implements DepositKrwPort {

    @Override
    public Optional<DepositId> depositKrw(ExchangeRequest exchangeRequest) {
        return Optional.of(new DepositId(UUID.randomUUID().toString()));
    }
}
