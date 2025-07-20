package com.boldfaced7.fxexchange.exchange.adapter.out.external.account;

import com.boldfaced7.fxexchange.common.ExternalSystemAdapter;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@ExternalSystemAdapter
public class KrwAccountHttpClient implements
        LoadTransactionClient,
        RequestTransactionClient
{
    private static final String DEPOSIT_PATH = "/api/v1/deposits";
    private static final String WITHDRAWAL_PATH = "/api/v1/withdrawals";

    private final WebClient webClient;

    public KrwAccountHttpClient(
            @Qualifier("krwWebClient") WebClient webClient
    ) {
        this.webClient = webClient;
    }

    @Override
    public Mono<TransactionResponse> loadDepositResult(ExchangeId exchangeId) {
        return loadTransactionResult(DEPOSIT_PATH, exchangeId);
    }

    @Override
    public Mono<TransactionResponse> loadWithdrawalResult(ExchangeId exchangeId) {
        return loadTransactionResult(WITHDRAWAL_PATH, exchangeId);
    }

    @Override
    public Mono<TransactionResponse> requestDeposit(ExchangeRequest exchangeRequest) {
        return requestTransaction(DEPOSIT_PATH, exchangeRequest, KrwDepositRequest::new);
    }

    @Override
    public Mono<TransactionResponse> requestWithdrawal(ExchangeRequest exchangeRequest) {
        return requestTransaction(WITHDRAWAL_PATH, exchangeRequest, KrwWithdrawalRequest::new);
    }

    private Mono<TransactionResponse> loadTransactionResult(String path, ExchangeId exchangeId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .path("/{exchangeId}")
                        .build(exchangeId.value()))
                .retrieve()
                .bodyToMono(TransactionResponse.class)
                .doOnSuccess(response -> log.info("원화 입출금 결과 조회({}/{}) 성공", path, exchangeId.value()))
                .doOnError(error -> log.error("원화 입출금 결과 조회({}/{}) 실패", path, exchangeId.value(), error.getMessage()));
    }

    private <T> Mono<TransactionResponse> requestTransaction(
            String path,
            ExchangeRequest exchangeRequest,
            Function<ExchangeRequest, T> constructor
    ) {
        return webClient.post()
                .uri(path)
                .bodyValue(constructor.apply(exchangeRequest))
                .retrieve()
                .bodyToMono(TransactionResponse.class)
                .doOnSuccess(response -> log.info("원화 입출금 요청({}/{}) 성공", path, exchangeRequest.getExchangeId()))
                .doOnError(error -> log.error("원화 입출금 요청({}/{}) 실패", path, exchangeRequest.getExchangeId(), error.getMessage()));
    }

    private record KrwDepositRequest(
            String exchangeId,
            String userId,
            String currency,
            int amount
    ) {
        public KrwDepositRequest(ExchangeRequest request) {
            this(
                    request.getExchangeId().value(),
                    request.getUserId().value(),
                    request.getBaseCurrency().value().toString(),
                    request.getBaseAmount().value()
            );
        }
    }

    private record KrwWithdrawalRequest(
            String exchangeId,
            String userId,
            String currency,
            int amount
    ) {
        public KrwWithdrawalRequest(ExchangeRequest request) {
            this(
                    request.getExchangeId().value(),
                    request.getUserId().value(),
                    request.getBaseCurrency().value().toString(),
                    request.getBaseAmount().value()
            );
        }
    }

}
