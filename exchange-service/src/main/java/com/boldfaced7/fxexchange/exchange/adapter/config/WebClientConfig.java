package com.boldfaced7.fxexchange.exchange.adapter.config;

import com.boldfaced7.fxexchange.exchange.adapter.out.property.WebClientBaseUrlProperties;
import com.boldfaced7.fxexchange.exchange.application.exception.NetworkErrorException;
import com.boldfaced7.fxexchange.exchange.application.exception.ServerUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Configuration
public class WebClientConfig {

    private static final int CONNECT_TIMEOUT_MILLIS = 2000;
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration WRITE_TIMEOUT = Duration.ofSeconds(2);

    private final WebClientBaseUrlProperties accountBaseUrl;

    private final CircuitBreaker fxCircuitBreaker;
    private final CircuitBreaker krwCircuitBreaker;

    public WebClientConfig(
            WebClientBaseUrlProperties accountBaseUrl,
            @Qualifier("fxCircuitBreaker") CircuitBreaker fxCircuitBreaker,
            @Qualifier("krwCircuitBreaker") CircuitBreaker krwCircuitBreaker
    ) {
        this.accountBaseUrl = accountBaseUrl;
        this.fxCircuitBreaker = fxCircuitBreaker;
        this.krwCircuitBreaker = krwCircuitBreaker;
    }

    @Bean
    public WebClient fxWebClient() {
        return createWebClient(accountBaseUrl.fxAccountBaseUrl(), fxCircuitBreaker);
    }

    @Bean
    public WebClient krwWebClient() {
        return createWebClient(accountBaseUrl.krwAccountBaseUrl(), krwCircuitBreaker);
    }

    private WebClient createWebClient(String baseUrl, CircuitBreaker circuitBreaker) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .responseTimeout(READ_TIMEOUT)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT.toSeconds(), TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT.toSeconds(), TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .filter(errorHandlingFilter())
                .filter(circuitBreakerFilter(circuitBreaker))
                .build();
    }

    private ExchangeFilterFunction errorHandlingFilter() {
        return (request, next) -> next.exchange(request)
                .flatMap(response -> {
                    if (response.statusCode().is5xxServerError()) {
                        return response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new ServerUnavailableException(errorBody)));
                    }
                    return Mono.just(response);
                })
                .onErrorMap(this::mapToDomainException);
    }

    private Throwable mapToDomainException(Throwable error) {
        if (error instanceof WebClientResponseException ex) {
            return ex.getStatusCode().is5xxServerError()
                    ? new ServerUnavailableException(error.getMessage())
                    : new NetworkErrorException(error.getMessage());
        }
        if (error instanceof WebClientRequestException || error instanceof TimeoutException) {
            return new NetworkErrorException(error.getMessage());
        }
        return error;
    }

    private ExchangeFilterFunction circuitBreakerFilter(CircuitBreaker circuitBreaker) {
        return (request, next) -> next.exchange(request)
                .transform(CircuitBreakerOperator.of(circuitBreaker));
    }

}