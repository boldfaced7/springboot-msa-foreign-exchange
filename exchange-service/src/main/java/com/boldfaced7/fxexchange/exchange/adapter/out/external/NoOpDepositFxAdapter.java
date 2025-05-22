package com.boldfaced7.fxexchange.exchange.adapter.out.external;

import com.boldfaced7.fxexchange.common.ExternalSystemAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.out.DepositFxPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositId;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@ExternalSystemAdapter
@RequiredArgsConstructor
public class NoOpDepositFxAdapter implements DepositFxPort {

    @Override
    public Optional<DepositId> depositFx(ExchangeRequest exchangeRequest) {
        return Optional.of(new DepositId(UUID.randomUUID().toString()));
    }
}