package com.boldfaced7.fxexchange.exchange.adapter.out.external;

import com.boldfaced7.fxexchange.common.ExternalSystemAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.out.WithdrawFxPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawId;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@ExternalSystemAdapter
@RequiredArgsConstructor
public class NoOpWithdrawFxAdapter implements WithdrawFxPort {

    @Override
    public Optional<WithdrawId> withdrawFx(ExchangeRequest exchangeRequest) {
        return Optional.of(new WithdrawId(UUID.randomUUID().toString()));
    }
}
