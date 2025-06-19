package com.boldfaced7.fxexchange.exchange.adapter.out.external.account;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import reactor.core.publisher.Mono;

public interface RequestTransactionClient {
    Mono<TransactionResponse> requestWithdrawal(ExchangeRequest exchangeRequest);
    Mono<TransactionResponse> requestDeposit(ExchangeRequest exchangeRequest);
}
