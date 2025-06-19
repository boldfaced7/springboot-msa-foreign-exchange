package com.boldfaced7.fxexchange.exchange.adapter.out.external.account;

import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import reactor.core.publisher.Mono;

public interface LoadTransactionClient {
    Mono<TransactionResponse> loadWithdrawalResult(ExchangeId exchangeId);
    Mono<TransactionResponse> loadDepositResult(ExchangeId exchangeId);
}
