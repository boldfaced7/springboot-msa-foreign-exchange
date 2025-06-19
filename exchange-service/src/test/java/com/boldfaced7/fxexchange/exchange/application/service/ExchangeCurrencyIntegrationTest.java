package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.exchange.adapter.config.IntegrationTestKafkaConfig;
import com.boldfaced7.fxexchange.exchange.adapter.out.persistence.log.ExchangeStateLogJpaRepository;
import com.boldfaced7.fxexchange.exchange.adapter.out.persistence.log.ExchangeStateLogMapper;
import com.boldfaced7.fxexchange.exchange.adapter.out.persistence.request.ExchangeRequestJpaRepository;
import com.boldfaced7.fxexchange.exchange.adapter.out.persistence.request.ExchangeRequestMapper;
import com.boldfaced7.fxexchange.exchange.application.port.in.ExchangeCurrencyCommand;
import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.enums.ExchangeState;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeStateLog;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("integration-test")
@SpringBootTest
@Testcontainers
@Import({
        IntegrationTestKafkaConfig.class
})
class ExchangeCurrencyIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.3")
    );

    static MockWebServer mockWebServer;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        registry.add("external.fx-account.base-url", () -> baseUrl);
        registry.add("external.krw-account.base-url", () -> baseUrl);

        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.admin.properties.default.replication.factor", () -> "1");
    }

    @Autowired ExchangeCurrencyService exchangeCurrencyService;
    @Autowired ExchangeRequestJpaRepository exchangeRequestJpaRepository;
    @Autowired ExchangeStateLogJpaRepository exchangeStateLogJpaRepository;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired KafkaTemplate<String, String> kafkaTemplate;

    ExchangeCurrencyCommand command;
    TransactionTemplate transactionTemplate;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        command = new ExchangeCurrencyCommand(
                new ExchangeId("exchangeId"),
                new UserId("userId"),
                new BaseCurrency(CurrencyCode.USD),
                new BaseAmount(100),
                new QuoteAmount(130000),
                Direction.BUY,
                new ExchangeRate(1300.0)
        );
        transactionTemplate = new TransactionTemplate(transactionManager);
    }
    @AfterEach
    void deleteAll() {
        exchangeRequestJpaRepository.deleteAllInBatch();
        exchangeStateLogJpaRepository.deleteAllInBatch();
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
        mockSuccessfulWithdrawal(); // 1. 출금 성공
        mockSuccessfulDeposit();    // 2. 입금 성공

        // When
        exchangeCurrencyService.exchangeCurrency(command);

        // Then
        assertRequestAndStates(true, true, false, true,
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.EXCHANGE_CURRENCY_SUCCEEDED
        );
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 성공 확인 → 환전 성공")
    void 출금_성공_입금_결과_모름_입금_성공_확인_환전_성공() {
        // Given
        mockSuccessfulWithdrawal();    // 1. 출금 성공
        mockUnknownDeposit();          // 2. 입금 결과 모름
        mockSuccessfulDepositResult(); // 3. 입금 성공 확인

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertRequestAndStates(true, true, false, true,
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_SUCCEEDED
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 결과 확인 실패(1-3회) → 입금 성공 확인 → 환전 성공")
    void 출금_성공_입금_결과_모름_입금_결과_확인_실패_N회_입금_성공_확인_환전_성공(int failCount) {
        // Given
        mockSuccessfulWithdrawal();          // 1. 출금 성공
        mockUnknownDeposit();                // 2. 입금 결과 모름
        mockUnknownDepositResult(failCount); // 3. 입금 결과 확인 실패 N회
        mockSuccessfulDepositResult();       // 4. 입금 성공 확인

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertRequestAndStates(true, true, false, true,
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
        mockSuccessfulWithdrawal(); // 1. 출금 성공
        mockFailedDeposit();        // 2. 입금 실패

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertRequestAndStates(true, true, true, false,
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
        mockSuccessfulWithdrawal(); // 1. 출금 성공
        mockUnknownDeposit();       // 2. 입금 결과 모름
        mockFailedDepositResult();  // 3. 입금 실패 확인

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertRequestAndStates(true, true, true, false,
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED,
                ExchangeState.CANCELING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 결과 확인 실패(1-3회) → 입금 실패 확인 → 출금 취소 → 환전 실패")
    void 출금_성공_입금_결과_모름_입금_결과_확인_실패_N회_입금_실패_확인_출금_취소_환전_실패(int failCount) {
        // Given
        mockSuccessfulWithdrawal();          // 1. 출금 성공
        mockUnknownDeposit();                // 2. 입금 결과 모름
        mockUnknownDepositResult(failCount); // 3. 입금 결과 확인 실패 N회
        mockFailedDepositResult();           // 4. 입금 실패 확인

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertRequestAndStates(true, true, true, false,
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
        mockUnknownWithdrawal();          // 1. 출금 결과 모름
        mockSuccessfulWithdrawalResult(); // 2. 출금 성공 확인

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertRequestAndStates(true, true, true, false,
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED,
                ExchangeState.CANCELING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("출금 결과 모름 → 출금 결과 확인 실패(1-3회) → 출금 성공 확인 → 출금 취소 → 환전 실패")
    void 출금_결과_모름_출금_결과_확인_실패_N회_출금_성공_확인_출금_취소_환전_실패(int failCount) {
        // Given
        mockUnknownWithdrawal();                // 1. 출금 결과 모름
        mockUnknownWithdrawalResult(failCount); // 2. 출금 결과 확인 실패 N회
        mockSuccessfulWithdrawalResult();       // 3. 출금 성공 확인

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertRequestAndStates(true, true, true, false,
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
        mockFailedWithdrawal(); // 1. 출금 실패

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertRequestAndStates(true, false, false, false,
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 결과 모름 → 출금 실패 확인 → 환전 실패")
    void 출금_결과_모름_출금_실패_확인_환전_실패() {
        // Given
        mockUnknownWithdrawal();      // 1. 출금 결과 모름
        mockFailedWithdrawalResult(); // 2. 출금 실패 확인

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertRequestAndStates(true, false, false, false,
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("출금 결과 모름 → 출금 결과 확인 실패(1-3회) → 출금 실패 확인 → 환전 실패")
    void 출금_결과_모름_출금_결과_확인_실패_N회_출금_실패_확인_환전_실패(int failCount) {
        // Given
        mockUnknownWithdrawal();                // 1. 출금 결과 모름
        mockUnknownWithdrawalResult(failCount); // 2. 출금 결과 확인 실패 N회
        mockFailedWithdrawalResult();           // 3. 출금 실패 확인

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertRequestAndStates(true, false, false, false,
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
        mockUnknownWithdrawal();              // 1. 출금 결과 모름
        mockUnknownWithdrawalResult(4); // 2. 출금 결과 확인 실패 4회

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertRequestAndStates(false, false, false, false,
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED
        );
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 결과 확인 실패(4회) → 경고 메시지 발송")
    void 출금_성공_입금_결과_모름_입금_결과_확인_실패_4회_경고_메시지_발송() {
        // Given
        mockSuccessfulWithdrawal();        // 1. 출금 성공
        mockUnknownDeposit();              // 2. 입금 결과 모름
        mockUnknownDepositResult(4); // 3. 입금 결과 확인 실패 4회

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertRequestAndStates(false, true, false, false,
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED
        );
    }

    ExchangeRequest getExchangeRequest() {
        return transactionTemplate.execute(status ->
                exchangeRequestJpaRepository.findAll().stream().findAny()
                        .map(ExchangeRequestMapper::toDomain)
                        .orElseThrow(() -> new IllegalStateException("환전 요청을 찾을 수 없습니다.")));
    }

    List<ExchangeState> getExchangeStates() {
        return transactionTemplate.execute(status ->
                exchangeStateLogJpaRepository.findAll().stream()
                        .map(ExchangeStateLogMapper::toDomain)
                        .map(ExchangeStateLog::getState)
                        .toList()
        );
    }

    void mockSuccessfulWithdrawal() {
        // 출금 요청 (/api/v1/withdrawals)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"success\":true,\"status\":\"SUCCESS\",\"transactionId\":\"withdrawalId\"}")
                .addHeader("Content-Type", "application/json"));
    }

    void mockFailedWithdrawal() {
        // 출금 요청 (/api/v1/withdrawals)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"success\":false,\"status\":\"FAILED\",\"transactionId\":null}")
                .addHeader("Content-Type", "application/json"));
    }

    void mockUnknownWithdrawal() {
        // 출금 요청 (/api/v1/withdrawals) - 알 수 없음
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));
    }

    void mockSuccessfulWithdrawalResult() {
        // 출금 결과 조회 (/api/v1/withdrawals/{withdrawalId})
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"success\":true,\"status\":\"SUCCESS\",\"transactionId\":\"withdrawalId\"}")
                .addHeader("Content-Type", "application/json"));
    }

    void mockFailedWithdrawalResult() {
        // 출금 결과 조회 (/api/v1/withdrawals/{withdrawalId})
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"success\":false,\"status\":\"FAILED\",\"transactionId\":null}")
                .addHeader("Content-Type", "application/json"));
    }

    void mockUnknownWithdrawalResult(int count) {
        // 출금 결과 조회 (/api/v1/withdrawals/{withdrawalId}) - 알 수 없음
        for (int i = 0; i < count; i++) {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
                    .setBody("Internal Server Error"));
        }
    }

    void mockSuccessfulDeposit() {
        // 입금 요청 (/api/v1/deposits)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"success\":true,\"status\":\"SUCCESS\",\"transactionId\":\"depositId\"}")
                .addHeader("Content-Type", "application/json"));
    }

    void mockFailedDeposit() {
        // 입금 요청 (/api/v1/deposits)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"success\":false,\"status\":\"FAILED\",\"transactionId\":null}")
                .addHeader("Content-Type", "application/json"));
    }

    void mockUnknownDeposit() {
        // 입금 요청 (/api/v1/deposits) - 알 수 없음
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));
    }

    void mockSuccessfulDepositResult() {
        // 입금 결과 조회 (/api/v1/deposits/{depositId})
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"success\":true,\"status\":\"SUCCESS\",\"transactionId\":\"depositId\"}")
                .addHeader("Content-Type", "application/json"));
    }

    void mockFailedDepositResult() {
        // 입금 결과 조회 (/api/v1/deposits/{depositId})
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"success\":false,\"status\":\"FAILED\",\"transactionId\":null}")
                .addHeader("Content-Type", "application/json"));
    }

    void mockUnknownDepositResult(int count) {
        // 입금 결과 조회 (/api/v1/deposits/{depositId}) - 알 수 없음
        for (int i = 0; i < count; i++) {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
                    .setBody("Internal Server Error"));
        }
    }

    @KafkaListener(
            topics = "${kafka.topic.scheduler.scheduler-topic}",
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
                    "${kafka.topic.fx-account.withdrawal-cancel-request-topic}",
                    "${kafka.topic.krw-account.withdrawal-cancel-request-topic}"
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
                    "exchangeId", exchangeId,
                    "withdrawalCancelId", "withdrawalCancelId"
            ));
            kafkaTemplate.send(replyTopic, serialized);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ack.acknowledge();
    }

    void assertRequestAndStates(
            boolean isFinished,
            boolean hasWithdrawalId,
            boolean hasWithdrawalCancelId,
            boolean hasDepositId,
            ExchangeState... expectedStates
    ) {
        Awaitility.await().atMost(Durations.TEN_SECONDS).pollInterval(Durations.ONE_SECOND)
                .untilAsserted(() -> {

            var request = getExchangeRequest();
            assertThat(request).isNotNull();
            assertThat(request.isFinished()).isEqualTo(isFinished);
            assertThat(request.getWithdrawalId() != null).isEqualTo(hasWithdrawalId);
            assertThat(request.getWithdrawalCancelId() != null).isEqualTo(hasWithdrawalCancelId);
            assertThat(request.getDepositId() != null).isEqualTo(hasDepositId);

            var states = getExchangeStates();
            assertThat(states).contains(expectedStates);

        });
    }

}