package com.boldfaced7.fxexchange.exchange.adapter.out.external.account;

import com.boldfaced7.fxexchange.common.ExternalSystemAdapter;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@ExternalSystemAdapter
public class FxAccountHttpClient implements
        LoadTransactionClient,
        RequestTransactionClient
{

    private static final String DEPOSIT_PATH = "/api/v1/deposits";
    private static final String WITHDRAWAL_PATH = "/api/v1/withdrawals";
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration WRITE_TIMEOUT = Duration.ofSeconds(2);

    private final WebClient webClient;

    public FxAccountHttpClient(
            @Qualifier("fxDepositWebClient") WebClient webClient
    ) {
        this.webClient = webClient;
    }

    @Override
    public Mono<TransactionResponse> loadDepositResult(ExchangeId exchangeId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(DEPOSIT_PATH)
                        .path("/{exchangeId}")
                        .build(exchangeId.value()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(TransactionResponse.class)
                .timeout(READ_TIMEOUT);
    }

    @Override
    public Mono<TransactionResponse> loadWithdrawalResult(ExchangeId exchangeId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(WITHDRAWAL_PATH)
                        .path("/{exchangeId}")
                        .build(exchangeId.value()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(TransactionResponse.class)
                .timeout(READ_TIMEOUT);
    }

    @Override
    public Mono<TransactionResponse> requestWithdrawal(ExchangeRequest exchangeRequest) {
        return webClient.post()
                .uri(WITHDRAWAL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new FxDepositRequest(exchangeRequest))
                .retrieve()
                .bodyToMono(TransactionResponse.class)
                .timeout(WRITE_TIMEOUT);
    }

    @Override
    public Mono<TransactionResponse> requestDeposit(ExchangeRequest exchangeRequest) {
        return webClient.post()
                .uri(DEPOSIT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new FxDepositRequest(exchangeRequest))
                .retrieve()
                .bodyToMono(TransactionResponse.class)
                .timeout(WRITE_TIMEOUT);
    }

    private record FxDepositRequest(
            String exchangeId,
            String userId,
            String currency,
            int amount
    ) {
        public FxDepositRequest(ExchangeRequest request) {
            this(
                    request.getExchangeId().value(),
                    request.getUserId().value(),
                    request.getBaseCurrency().value().toString(),
                    request.getBaseAmount().value()
            );
        }
    }

}
