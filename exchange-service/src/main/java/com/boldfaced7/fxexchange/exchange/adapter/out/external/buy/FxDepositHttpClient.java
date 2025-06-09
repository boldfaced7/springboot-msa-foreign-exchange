package com.boldfaced7.fxexchange.exchange.adapter.out.external.buy;

import com.boldfaced7.fxexchange.common.ExternalSystemAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.out.LoadDepositResultPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.RequestDepositPort;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.AccountCommandStatus;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositId;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositResult;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
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
public class FxDepositHttpClient implements RequestDepositPort, LoadDepositResultPort {

    private static final String DEPOSIT_PATH = "/api/v1/deposits";
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration WRITE_TIMEOUT = Duration.ofSeconds(2);

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    public FxDepositHttpClient(
            @Qualifier("fxDepositWebClient") WebClient webClient,
            CircuitBreaker circuitBreaker
    ) {
        this.webClient = webClient;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public DepositResult loadDepositResult(ExchangeId exchangeId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(DEPOSIT_PATH)
                        .path("/{exchangeId}")
                        .build(exchangeId.value()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(FxDepositResponse.class)
                .map(FxDepositResponse::toDepositResult)
                .timeout(READ_TIMEOUT)
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                .block();
    }

    @Override
    public DepositResult deposit(ExchangeRequest request) {
        return webClient.post()
                .uri(DEPOSIT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(FxDepositResponse.class)
                .map(FxDepositResponse::toDepositResult)
                .timeout(WRITE_TIMEOUT)
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                .block();
    }

    @Override
    public Direction direction() {
        return Direction.BUY;
    }

    private record FxDepositResponse(
            boolean success,
            String status,
            String depositId
    ) {
        public DepositResult toDepositResult() {
            return new DepositResult(
                    success,
                    new AccountCommandStatus(status),
                    new DepositId(depositId)
            );
        }
    }
}
