package com.boldfaced7.fxexchange.exchange.adapter.out.external.sell;

import com.boldfaced7.fxexchange.common.ExternalSystemAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.out.LoadWithdrawalResultPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.RequestWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.AccountCommandStatus;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalResult;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Slf4j
@Profile("!test")
@ExternalSystemAdapter
public class FxWithdrawalHttpClient implements
        RequestWithdrawalPort,
        LoadWithdrawalResultPort
{
    private static final String WITHDRAWAL_PATH = "/api/v1/withdrawals";
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration WRITE_TIMEOUT = Duration.ofSeconds(2);

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    public FxWithdrawalHttpClient(
            @Qualifier("fxWithdrawalWebClient") WebClient webClient,
            CircuitBreaker circuitBreaker
    ) {
        this.webClient = webClient;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public WithdrawalResult loadWithdrawalResult(ExchangeId exchangeId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(WITHDRAWAL_PATH)
                        .path("/{exchangeId}")
                        .build(exchangeId.value()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(FxWithdrawalResponse.class)
                .map(FxWithdrawalResponse::toWithdrawalResult)
                .timeout(READ_TIMEOUT)
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                .block();
    }

    @Override
    public WithdrawalResult withdraw(ExchangeRequest requested) {
        return webClient.post()
                .uri(WITHDRAWAL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requested)
                .retrieve()
                .bodyToMono(FxWithdrawalResponse.class)
                .map(FxWithdrawalResponse::toWithdrawalResult)
                .timeout(WRITE_TIMEOUT)
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                .block();
    }

    @Override
    public Direction direction() {
        return Direction.SELL;
    }

    private record FxWithdrawalResponse(
            boolean success,
            String status,
            String withdrawalId
    ) {
        public WithdrawalResult toWithdrawalResult() {
            return new WithdrawalResult(
                    success,
                    new AccountCommandStatus(status),
                    new WithdrawalId(withdrawalId)
            );
        }
    }
}
