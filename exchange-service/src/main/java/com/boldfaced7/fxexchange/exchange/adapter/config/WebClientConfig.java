package com.boldfaced7.fxexchange.exchange.adapter.config;

import com.boldfaced7.fxexchange.exchange.application.exception.NetworkErrorException;
import com.boldfaced7.fxexchange.exchange.application.exception.ServerUnavailableException;
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

    private static final Map<String, String> SERVICE_ERROR_MESSAGES = Map.of(
            "fxDeposit", "외화 입금 서비스 장애",
            "krwWithdrawal", "원화 출금 서비스 장애",
            "fxWithdrawal", "외화 출금 서비스 장애",
            "krwDeposit", "원화 입금 서비스 장애"
    );

    @Value("${external.fx-account.base-url}")
    private String fxAccountBaseUrl;

    @Value("${external.krw-account.base-url}")
    private String krwAccountBaseUrl;

    @Bean
    public WebClient fxDepositWebClient() {
        return createWebClient(fxAccountBaseUrl, "fxDeposit");
    }

    @Bean
    public WebClient krwWithdrawalWebClient() {
        return createWebClient(krwAccountBaseUrl, "krwWithdrawal");
    }

    @Bean
    public WebClient fxWithdrawalWebClient() {
        return createWebClient(fxAccountBaseUrl, "fxWithdrawal");
    }

    @Bean
    public WebClient krwDepositWebClient() {
        return createWebClient(krwAccountBaseUrl, "krwDeposit");
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
} 