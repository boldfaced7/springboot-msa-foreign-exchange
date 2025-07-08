package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.exchange.adapter.out.cache.RedisExchangeRequestRepository;
import com.boldfaced7.fxexchange.exchange.adapter.out.persistence.cancel.withdrawal.WithdrawalCancelJpaRepository;
import com.boldfaced7.fxexchange.exchange.adapter.out.persistence.cancel.withdrawal.WithdrawalCancelMapper;
import com.boldfaced7.fxexchange.exchange.adapter.out.persistence.deposit.DepositJpaRepository;
import com.boldfaced7.fxexchange.exchange.adapter.out.persistence.deposit.DepositMapper;
import com.boldfaced7.fxexchange.exchange.adapter.out.persistence.exchange.ExchangeRequestJpaRepository;
import com.boldfaced7.fxexchange.exchange.adapter.out.persistence.exchange.ExchangeRequestMapper;
import com.boldfaced7.fxexchange.exchange.adapter.out.persistence.log.ExchangeStateLogJpaRepository;
import com.boldfaced7.fxexchange.exchange.adapter.out.persistence.log.ExchangeStateLogMapper;
import com.boldfaced7.fxexchange.exchange.adapter.out.persistence.withdrawal.WithdrawalJpaRepository;
import com.boldfaced7.fxexchange.exchange.adapter.out.persistence.withdrawal.WithdrawalMapper;
import com.boldfaced7.fxexchange.exchange.application.util.TestContainerSupport;
import com.boldfaced7.fxexchange.exchange.domain.enums.ExchangeState;
import com.boldfaced7.fxexchange.exchange.domain.model.*;
import com.boldfaced7.fxexchange.exchange.domain.vo.Deposited;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeFinished;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalCancelled;
import com.boldfaced7.fxexchange.exchange.domain.vo.Withdrawn;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.boldfaced7.fxexchange.exchange.application.util.TestConstraints.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("integration-test")
@SpringBootTest
class ExchangeCurrencyServiceIntegrationTest extends TestContainerSupport {

    static MockWebServer mockWebServer;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        registry.add("web-client.fx-account-base-url", () -> baseUrl);
        registry.add("web-client.krw-account-base-url", () -> baseUrl);
    }

    @Autowired ExchangeCurrencyService exchangeCurrencyService;

    @Autowired ExchangeRequestJpaRepository exchangeRequestJpaRepository;
    @Autowired ExchangeStateLogJpaRepository exchangeStateLogJpaRepository;
    @Autowired DepositJpaRepository depositJpaRepository;
    @Autowired WithdrawalJpaRepository withdrawalJpaRepository;
    @Autowired WithdrawalCancelJpaRepository withdrawalCancelJpaRepository;
    @Autowired RedisExchangeRequestRepository redisExchangeRequestRepository;
    @Autowired RedisConnectionFactory redisConnectionFactory;
    @Autowired KafkaTemplate<String, String> kafkaTemplate;

    @Autowired ObjectMapper objectMapper;

    @AfterEach
    void deleteAll() {
        exchangeRequestJpaRepository.deleteAllInBatch();
        exchangeStateLogJpaRepository.deleteAllInBatch();
        depositJpaRepository.deleteAllInBatch();
        withdrawalJpaRepository.deleteAllInBatch();
        withdrawalCancelJpaRepository.deleteAllInBatch();
        redisExchangeRequestRepository.deleteAll();
        redisConnectionFactory.getConnection().serverCommands().flushAll();

        mockWebServer.setDispatcher(new QueueDispatcher());
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
        mysql.close();
        kafka.close();
    }

    // 1. 출금 성공
    // 1.1. 입금 성공
    @Test
    @DisplayName("출금 성공 → 입금 성공 → 환전 성공")
    void 출금_성공_입금_성공_환전_성공() {
        // Given
        enqueueResponse(WITHDRAWAL_SUCCESS);
        enqueueResponse(DEPOSIT_SUCCESS);

        // When
        exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND);

        // Then
        assertModelsAndStates(
                ExchangeFinished.FINISHED,
                Withdrawn.WITHDRAWN,
                Deposited.DEPOSITED,
                WithdrawalCancelled.NOT_CANCELLED,

                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.EXCHANGE_CURRENCY_SUCCEEDED
        );
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 성공 확인 → 환전 성공")
    void 출금_성공_입금_결과_모름_입금_성공_확인_환전_성공() {
        // Given
        enqueueResponse(WITHDRAWAL_SUCCESS);
        enqueueErrorResponse(1);
        enqueueResponse(DEPOSIT_SUCCESS);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertModelsAndStates(
                ExchangeFinished.FINISHED,
                Withdrawn.WITHDRAWN,
                Deposited.DEPOSITED,
                WithdrawalCancelled.NOT_CANCELLED,

                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_SUCCEEDED
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("failureCountArguments")
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 결과 확인 실패(1-3회) → 입금 성공 확인 → 환전 성공")
    void 출금_성공_입금_결과_모름_입금_결과_확인_실패_N회_입금_성공_확인_환전_성공(String message, int failCount) {
        // Given
        enqueueResponse(WITHDRAWAL_SUCCESS);
        enqueueErrorResponse(failCount);
        enqueueResponse(DEPOSIT_SUCCESS);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertModelsAndStates(
                ExchangeFinished.FINISHED,
                Withdrawn.WITHDRAWN,
                Deposited.DEPOSITED,
                WithdrawalCancelled.NOT_CANCELLED,

                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_SUCCEEDED
        );
    }

    // 1.2. 입금 실패
    @Test
    @DisplayName("출금 성공 → 입금 실패 → 출금 취소 → 환전 실패")
    void 출금_성공_입금_실패_출금_취소_환전_실패() {
        // Given
        enqueueResponse(WITHDRAWAL_SUCCESS);
        enqueueResponse(DEPOSIT_FAILED);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertModelsAndStates(
                ExchangeFinished.FINISHED,
                Withdrawn.WITHDRAWN,
                Deposited.NOT_DEPOSITED,
                WithdrawalCancelled.CANCELLED,

                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CANCELING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 실패 확인 → 출금 취소 → 환전 실패")
    void 출금_성공_입금_결과_모름_입금_실패_확인_출금_취소_환전_실패() {
        // Given
        enqueueResponse(WITHDRAWAL_SUCCESS);
        enqueueErrorResponse(1);
        enqueueResponse(DEPOSIT_FAILED);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertModelsAndStates(
                ExchangeFinished.FINISHED,
                Withdrawn.WITHDRAWN,
                Deposited.NOT_DEPOSITED,
                WithdrawalCancelled.CANCELLED,

                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED,
                ExchangeState.CANCELING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("failureCountArguments")
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 결과 확인 실패(1-3회) → 입금 실패 확인 → 출금 취소 → 환전 실패")
    void 출금_성공_입금_결과_모름_입금_결과_확인_실패_N회_입금_실패_확인_출금_취소_환전_실패(String message, int failCount) {
        // Given
        enqueueResponse(WITHDRAWAL_SUCCESS);
        enqueueErrorResponse(failCount);
        enqueueResponse(DEPOSIT_FAILED);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertModelsAndStates(
                ExchangeFinished.FINISHED,
                Withdrawn.WITHDRAWN,
                Deposited.NOT_DEPOSITED,
                WithdrawalCancelled.CANCELLED,

                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED,
                ExchangeState.CANCELING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 결과 모름 → 출금 성공 확인 → 출금 취소 → 환전 실패")
    void 출금_결과_모름_출금_성공_확인_출금_취소_환전_실패() {
        // Given
        enqueueErrorResponse(1);
        enqueueResponse(WITHDRAWAL_SUCCESS);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertModelsAndStates(
                ExchangeFinished.FINISHED,
                Withdrawn.WITHDRAWN,
                Deposited.NOT_DEPOSITED,
                WithdrawalCancelled.CANCELLED,

                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED,
                ExchangeState.CANCELING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("failureCountArguments")
    @DisplayName("출금 결과 모름 → 출금 결과 확인 실패(1-3회) → 출금 성공 확인 → 출금 취소 → 환전 실패")
    void 출금_결과_모름_출금_결과_확인_실패_N회_출금_성공_확인_출금_취소_환전_실패(String message, int failCount) {
        // Given
        enqueueErrorResponse(failCount);
        enqueueResponse(WITHDRAWAL_SUCCESS);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertModelsAndStates(
                ExchangeFinished.FINISHED,
                Withdrawn.WITHDRAWN,
                Deposited.NOT_DEPOSITED,
                WithdrawalCancelled.CANCELLED,

                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED,
                ExchangeState.CANCELING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    // 2. 출금 실패
    @Test
    @DisplayName("출금 실패 → 환전 실패")
    void 출금_실패_환전_실패() {
        // Given
        enqueueResponse(WITHDRAWAL_FAILED);
        enqueueErrorResponse(1);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertModelsAndStates(
                ExchangeFinished.FINISHED,
                Withdrawn.NOT_WITHDRAWN,
                Deposited.NOT_DEPOSITED,
                WithdrawalCancelled.NOT_CANCELLED,

                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 결과 모름 → 출금 실패 확인 → 환전 실패")
    void 출금_결과_모름_출금_실패_확인_환전_실패() {
        // Given
        enqueueErrorResponse(1);
        enqueueResponse(WITHDRAWAL_FAILED);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertModelsAndStates(
                ExchangeFinished.FINISHED,
                Withdrawn.NOT_WITHDRAWN,
                Deposited.NOT_DEPOSITED,
                WithdrawalCancelled.NOT_CANCELLED,

                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("failureCountArguments")
    @DisplayName("출금 결과 모름 → 출금 결과 확인 실패(1-3회) → 출금 실패 확인 → 환전 실패")
    void 출금_결과_모름_출금_결과_확인_실패_N회_출금_실패_확인_환전_실패(String message, int failCount) {
        // Given
        enqueueErrorResponse(failCount);
        enqueueResponse(WITHDRAWAL_FAILED);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertModelsAndStates(
                ExchangeFinished.FINISHED,
                Withdrawn.NOT_WITHDRAWN,
                Deposited.NOT_DEPOSITED,
                WithdrawalCancelled.NOT_CANCELLED,

                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    // 3. 입출금 결과 확인 실패(4회)
    @Test
    @DisplayName("출금 결과 모름 → 출금 결과 확인 실패(4회) → 경고 메시지 발송")
    void 출금_결과_모름_출금_결과_확인_실패_4회_경고_메시지_발송() {
        // Given
        enqueueErrorResponse(1 + 4);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertModelsAndStates(
                ExchangeFinished.NOT_FINISHED,
                Withdrawn.NOT_WITHDRAWN,
                Deposited.NOT_DEPOSITED,
                WithdrawalCancelled.NOT_CANCELLED,

                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED
        );
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 결과 확인 실패(4회) → 경고 메시지 발송")
    void 출금_성공_입금_결과_모름_입금_결과_확인_실패_4회_경고_메시지_발송() {
        // Given
        enqueueResponse(WITHDRAWAL_SUCCESS);
        enqueueErrorResponse(1 + 4);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertModelsAndStates(
                ExchangeFinished.NOT_FINISHED,
                Withdrawn.WITHDRAWN,
                Deposited.NOT_DEPOSITED,
                WithdrawalCancelled.NOT_CANCELLED,

                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED
        );
    }

    void enqueueResponse(Object body) {
        try {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody(objectMapper.writeValueAsString(body))
                    .addHeader("Content-Type", "application/json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void enqueueErrorResponse(int count) {
        for (int i = 0; i < count; i++) {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
                    .setBody("Internal Server Error")
            );
        }
    }

    static Stream<Arguments> failureCountArguments() {
        return Stream.of(
                Arguments.of("1회", 1 + 1),
                Arguments.of("2회", 1 + 2),
                Arguments.of("3회", 1 + 3)
        );
    }



    @KafkaListener(
            topics = "${kafka.scheduler.scheduler-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    void mockScheduler(
            @Header(KafkaHeaders.REPLY_TOPIC) String replyTopic,
            @Payload String payload,
            Acknowledgment ack
    ) {
        kafkaTemplate.send(replyTopic, payload);
        ack.acknowledge();
    }

    @KafkaListener(
            topics = {
                    "${kafka.fx-account.withdrawal-cancel-request-topic}",
                    "${kafka.krw-account.withdrawal-cancel-request-topic}"
            },
            groupId = "${spring.kafka.consumer.group-id}"
    )
    void mockAccountWithdrawalCancel(
            @Header(KafkaHeaders.REPLY_TOPIC) String replyTopic,
            @Payload String payload,
            Acknowledgment ack
    ) {
        try {
            var exchangeId = objectMapper.readValue(payload, Map.class).get("exchangeId");
            var serialized = objectMapper.writeValueAsString(Map.of(
                    "withdrawalCancelId", "withdrawalCancelId",
                    "exchangeId", exchangeId
            ));
            kafkaTemplate.send(replyTopic, serialized);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ack.acknowledge();
    }

    void assertModelsAndStates(
            ExchangeFinished exchangeFinished,
            Withdrawn withdrawn,
            Deposited deposited,
            WithdrawalCancelled withdrawalCancelled,
            ExchangeState... expectedStates
    ) {
        Awaitility.await().atMost(Durations.TEN_SECONDS).pollInterval(Durations.ONE_SECOND)
                .untilAsserted(() -> {
                    var request = getExchangeRequest();
                    assertThat(request).isNotNull();
                    assertThat(request.isFinished()).isEqualTo(exchangeFinished.value());

                    var withdrawal = getWithdrawal();
                    assertThat(withdrawal.isPresent()).isEqualTo(withdrawn.value());

                    var deposit = getDeposit();
                    assertThat(deposit.isPresent()).isEqualTo(deposited.value());

                    var withdrawalCancel = getWithdrawalCancel();
                    assertThat(withdrawalCancel.isPresent()).isEqualTo(withdrawalCancelled.value());

                    var states = getExchangeStates();
                    assertThat(states).contains(expectedStates);
                });
    }

    ExchangeRequest getExchangeRequest() {
        return exchangeRequestJpaRepository.findAll().stream().findAny()
                .map(ExchangeRequestMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("환전 요청을 찾을 수 없습니다."));
    }

    List<ExchangeState> getExchangeStates() {
        return exchangeStateLogJpaRepository.findAll().stream()
                .map(ExchangeStateLogMapper::toDomain)
                .map(ExchangeStateLog::getState)
                .toList();
    }

    Optional<Deposit> getDeposit() {
        return depositJpaRepository.findAll().stream().findAny()
                .map(DepositMapper::toDomain);
    }

    Optional<Withdrawal> getWithdrawal() {
        return withdrawalJpaRepository.findAll().stream().findAny()
                .map(WithdrawalMapper::toDomain);
    }

    Optional<WithdrawalCancel> getWithdrawalCancel() {
        return withdrawalCancelJpaRepository.findAll().stream().findAny()
                .map(WithdrawalCancelMapper::toDomain);
    }

}