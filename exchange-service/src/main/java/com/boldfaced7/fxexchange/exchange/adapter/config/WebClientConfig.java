package com.boldfaced7.fxexchange.exchange.adapter.config;

import com.boldfaced7.fxexchange.exchange.application.exception.NetworkErrorException;
import com.boldfaced7.fxexchange.exchange.application.exception.ServerUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Configuration
public class WebClientConfig {

    // 타임아웃 설정
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration WRITE_TIMEOUT = Duration.ofSeconds(2);

    // 서킷브레이커 설정
    private static final int CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD = 50;
    private static final int CIRCUIT_BREAKER_MINIMUM_NUMBER_OF_CALLS = 10;
    private static final Duration CIRCUIT_BREAKER_WAIT_DURATION = Duration.ofSeconds(10);
    private static final int CIRCUIT_BREAKER_PERMITTED_NUMBER_OF_CALLS_IN_HALF_OPEN_STATE = 5;

    private static final Map<String, String> SERVICE_ERROR_MESSAGES = Map.of(
            "fxDeposit", "외화 입금 서비스 장애",
            "krwWithdrawal", "원화 출금 서비스 장애",
            "fxWithdrawal", "외화 출금 서비스 장애",
            "krwDeposit", "원화 입금 서비스 장애"
    );

    @Value("${external.fx-deposit.base-url}")
    private String fxDepositBaseUrl;

    @Value("${external.krw-withdrawal.base-url}")
    private String krwWithdrawalBaseUrl;

    @Value("${external.fx-withdrawal.base-url}")
    private String fxWithdrawalBaseUrl;

    @Value("${external.krw-deposit.base-url}")
    private String krwDepositBaseUrl;

    @Bean
    public WebClient fxDepositWebClient() {
        return createWebClient(fxDepositBaseUrl, "fxDeposit");
    }

    @Bean
    public WebClient krwWithdrawalWebClient() {
        return createWebClient(krwWithdrawalBaseUrl, "krwWithdrawal");
    }

    @Bean
    public WebClient fxWithdrawalWebClient() {
        return createWebClient(fxWithdrawalBaseUrl, "fxWithdrawal");
    }

    @Bean
    public WebClient krwDepositWebClient() {
        return createWebClient(krwDepositBaseUrl, "krwDeposit");
    }

    private WebClient createWebClient(String baseUrl, String serviceName) {
        HttpClient httpClient = HttpClient.create()
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) CONNECT_TIMEOUT.toMillis())
                .responseTimeout(READ_TIMEOUT)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler((int) READ_TIMEOUT.toSeconds()))
                        .addHandlerLast(new WriteTimeoutHandler((int) WRITE_TIMEOUT.toSeconds())));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(errorHandlingFilter(serviceName))
                .build();
    }

    private ExchangeFilterFunction errorHandlingFilter(String serviceName) {
        return (request, next) -> next.exchange(request)
                .flatMap(response -> {
                    if (response.statusCode().is5xxServerError()) {
                        return response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new ServerUnavailableException(
                                        SERVICE_ERROR_MESSAGES.get(serviceName) + ": " + errorBody)));
                    }
                    return Mono.just(response);
                })
                .onErrorMap(this::mapToDomainException);
    }

    private Throwable mapToDomainException(Throwable error) {
        if (error instanceof WebClientResponseException ex) {
            return ex.getStatusCode().is5xxServerError()
                    ? new ServerUnavailableException("서버 오류: " + ex.getMessage())
                    : new NetworkErrorException("응답 오류: " + ex.getMessage());
        }
        if (error instanceof WebClientRequestException || error instanceof java.util.concurrent.TimeoutException) {
            return new NetworkErrorException("통신 실패: " + error.getMessage());
        }
        return error;
    }

    @Bean
    public CircuitBreaker circuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD)
                .minimumNumberOfCalls(CIRCUIT_BREAKER_MINIMUM_NUMBER_OF_CALLS)
                .waitDurationInOpenState(CIRCUIT_BREAKER_WAIT_DURATION)
                .permittedNumberOfCallsInHalfOpenState(CIRCUIT_BREAKER_PERMITTED_NUMBER_OF_CALLS_IN_HALF_OPEN_STATE)
                .recordExceptions(Exception.class)
                .build();

        return CircuitBreaker.of("webclient-circuit-breaker", config);
    }
} 