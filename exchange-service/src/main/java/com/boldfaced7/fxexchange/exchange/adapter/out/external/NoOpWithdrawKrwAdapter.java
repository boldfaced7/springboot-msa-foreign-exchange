package com.boldfaced7.fxexchange.exchange.adapter.out.external;

import com.boldfaced7.fxexchange.common.ExternalSystemAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.out.WithdrawKrwPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawId;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@ExternalSystemAdapter
@RequiredArgsConstructor
public class NoOpWithdrawKrwAdapter implements WithdrawKrwPort {

    @Override
    public Optional<WithdrawId> withdrawKrw(ExchangeRequest exchangeRequest) {
        return Optional.of(new WithdrawId(UUID.randomUUID().toString()));
    }
}
