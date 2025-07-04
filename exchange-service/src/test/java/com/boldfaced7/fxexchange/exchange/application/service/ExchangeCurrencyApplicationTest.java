package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.exchange.adapter.test.ExchangeRequestPersistenceAdapterForTest;
import com.boldfaced7.fxexchange.exchange.adapter.test.ExchangeStateLogPersistenceAdapterForTest;
import com.boldfaced7.fxexchange.exchange.adapter.test.account.*;
import com.boldfaced7.fxexchange.exchange.application.config.ApplicationTestPortConfig;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckDepositWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckWithdrawalWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.CompleteWithdrawalCancelCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.ExchangeCurrencyCommand;
import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.enums.ExchangeState;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeStateLog;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest(classes = ApplicationTestPortConfig.class)
@ActiveProfiles("application-test")
class ExchangeCurrencyApplicationTest {

    @Autowired ExchangeCurrencyService exchangeCurrencyService;
    @Autowired CheckDepositWithDelayService checkDepositWithDelayService;
    @Autowired CheckWithdrawalWithDelayService checkWithdrawalWithDelayService;
    @Autowired CompleteWithdrawalCancelService completeWithdrawalCancelService;

    @Autowired ExchangeRequestPersistenceAdapterForTest exchangeRequestPersistenceAdapter;
    @Autowired ExchangeStateLogPersistenceAdapterForTest exchangeStateLogPersistenceAdapter;
    @Autowired RequestDepositPortForTest requestDepositPort;
    @Autowired RequestWithdrawalPortForTest requestWithdrawalPort;
    @Autowired LoadDepositResultPortForTest loadDepositResultPort;
    @Autowired LoadWithdrawalResultPortForTest loadWithdrawalResultPort;
    @Autowired ScheduleCheckRequestPortForTest scheduleCheckRequestPort;
    @Autowired CancelWithdrawalPortForTest cancelWithdrawalPort;

    ExchangeCurrencyCommand command;

    Consumer<Integer> checkDepositWithDelay = count ->
            checkDepositWithDelayService.checkDepositWithDelay(
                    new CheckDepositWithDelayCommand(
                            command.exchangeId(),
                            new Count(count),
                            Direction.BUY
                    )
            );

    Consumer<Integer> checkWithdrawalWithDelay = count ->
            checkWithdrawalWithDelayService.checkWithdrawalWithDelay(
                    new CheckWithdrawalWithDelayCommand(
                            command.exchangeId(),
                            new Count(count),
                            Direction.BUY
                    )
            );

    Runnable completeWithdrawalCancel = () ->
            completeWithdrawalCancelService.completeWithdrawalCancel(
                    new CompleteWithdrawalCancelCommand(
                            command.exchangeId(),
                            new WithdrawalCancelId("withdrawalCancelId"),
                            Direction.BUY
                    )
            );

    @BeforeEach
    void setUp() {
        command = new ExchangeCurrencyCommand(
                new UserId("userId"),
                new BaseCurrency(CurrencyCode.USD),
                new BaseAmount(100),
                new QuoteAmount(130000),
                Direction.BUY,
                new ExchangeRate(1300.0)
        );

        // 모든 테스트 포트 초기화
        requestDepositPort.reset();
        requestWithdrawalPort.reset();

        loadDepositResultPort.reset();
        loadWithdrawalResultPort.reset();

        scheduleCheckRequestPort.reset();

        cancelWithdrawalPort.reset();

        exchangeRequestPersistenceAdapter.reset();
        exchangeStateLogPersistenceAdapter.reset();
    }

    ExchangeRequest getExchangeRequest(RequestId requestId) {
        return exchangeRequestPersistenceAdapter.loadByRequestId(requestId);
    }

    List<ExchangeState> getExchangeStates(RequestId requestId) {
        return exchangeStateLogPersistenceAdapter.findByRequestId(requestId)
                .stream()
                .map(ExchangeStateLog::getState)
                .toList();
    }

    RequestId getRequestId() {
        return exchangeRequestPersistenceAdapter.getRequestId();
    }

    void sleepForAsync(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 1. 출금 성공
    // 1.1. 입금 성공
    @Test
    @DisplayName("출금 성공 → 입금 성공 → 환전 성공")
    void 출금_성공_입금_성공_환전_성공() {
        // Given
        // 1. 출금 성공
        requestWithdrawalPort.setSuccess(
                command.exchangeId(), "SUCCESS", "withdrawalId");

        // 2. 입금 성공
        requestDepositPort.setSuccess(
                command.exchangeId(), "SUCCESS", "depositId");

        // When
        ExchangeDetail result = exchangeCurrencyService.exchangeCurrency(command);

        // Then
        sleepForAsync(1000);
        ExchangeRequest exchangeRequest = result.exchangeRequest();

        assertThat(exchangeRequest.isFinished()).isTrue();
        assertThat(exchangeRequest.getWithdrawalId()).isNotNull();
        assertThat(exchangeRequest.getWithdrawalCancelId()).isNull();
        assertThat(exchangeRequest.getDepositId()).isNotNull();

        List<ExchangeState> logs = getExchangeStates(exchangeRequest.getRequestId());

        assertThat(logs).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.EXCHANGE_CURRENCY_SUCCEEDED
        );
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 성공 확인 → 환전 성공")
    void 입금_성공_확인_후_환전_성공() {
        // Given
        // 1. 출금 성공
        requestWithdrawalPort.setSuccess(
                command.exchangeId(), "SUCCESS", "withdrawalId");

        // 2. 입금 결과 모름
        requestDepositPort.setThrowException(command.exchangeId());

        // 3. 입금 성공 확인
        loadDepositResultPort.setSuccess(
                command.exchangeId(), "SUCCESS", "depositId");

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);


        // Then
        sleepForAsync(1000);

        RequestId requestId = getRequestId();
        List<ExchangeState> logs = getExchangeStates(requestId);
        ExchangeRequest result = getExchangeRequest(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNotNull();
        assertThat(result.getWithdrawalCancelId()).isNull();
        assertThat(result.getDepositId()).isNotNull();

        assertThat(logs).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_SUCCEEDED
        );
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 결과 확인 실패(1회) → 입금 성공 확인 → 환전 성공")
    void 입금_결과_확인_실패_1회_후_환전_성공() {
        // Given
        // 1. 출금 성공
        requestWithdrawalPort.setSuccess(
                command.exchangeId(), "SUCCESS", "withdrawalId");

        // 2. 입금 결과 모름
        requestDepositPort.setThrowException(command.exchangeId());

        // 3. 입금 결과 확인 실패 1회
        loadDepositResultPort.setThrowException(command.exchangeId());

        // 4. 입금 결과 조회 지연
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkDepositWithDelay.accept(1));

        // 5. 입금 성공 확인
        loadDepositResultPort.setSuccess(
                command.exchangeId(), "SUCCESS", "depositId");

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);

        RequestId requestId = getRequestId();
        List<ExchangeState> logs = getExchangeStates(requestId);
        ExchangeRequest result = getExchangeRequest(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNotNull();
        assertThat(result.getWithdrawalCancelId()).isNull();
        assertThat(result.getDepositId()).isNotNull();

        assertThat(logs).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_SUCCEEDED
        );
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 결과 확인 실패(2회) → 입금 성공 확인 → 환전 성공")
    void 입금_결과_확인_실패_2회_후_환전_성공() {
        // Given
        // 1. 출금 성공
        requestWithdrawalPort.setSuccess(
                command.exchangeId(), "SUCCESS", "withdrawalId");

        // 2. 입금 결과 모름
        requestDepositPort.setThrowException(command.exchangeId());

        // 3. 입금 결과 확인 실패 2회
        loadDepositResultPort.setThrowException(command.exchangeId());
        loadDepositResultPort.setThrowException(command.exchangeId());

        // 4. 입금 결과 조회 지연 2회
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkDepositWithDelay.accept(1));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkDepositWithDelay.accept(2));

        // 5. 입금 성공 확인
        loadDepositResultPort.setSuccess(
                command.exchangeId(), "SUCCESS", "depositId");

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);

        RequestId requestId = getRequestId();
        List<ExchangeState> logs = getExchangeStates(requestId);
        ExchangeRequest result = getExchangeRequest(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNotNull();
        assertThat(result.getWithdrawalCancelId()).isNull();
        assertThat(result.getDepositId()).isNotNull();

        assertThat(logs).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_SUCCEEDED
        );
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 결과 확인 실패(3회) → 입금 성공 확인 → 환전 성공")
    void 입금_결과_확인_실패_3회_후_환전_성공() {
        // Given
        // 1. 출금 성공
        requestWithdrawalPort.setSuccess(
                command.exchangeId(), "SUCCESS", "withdrawalId");

        // 2. 입금 결과 모름
        requestDepositPort.setThrowException(command.exchangeId());

        // 3. 입금 결과 확인 실패 3회
        loadDepositResultPort.setThrowException(command.exchangeId());
        loadDepositResultPort.setThrowException(command.exchangeId());
        loadDepositResultPort.setThrowException(command.exchangeId());

        // 4. 입금 결과 조회 지연 3회
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkDepositWithDelay.accept(1));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkDepositWithDelay.accept(2));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkDepositWithDelay.accept(3));

        // 5. 입금 성공 확인
        loadDepositResultPort.setSuccess(
                command.exchangeId(), "SUCCESS", "depositId");

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);

        RequestId requestId = getRequestId();
        List<ExchangeState> logs = getExchangeStates(requestId);
        ExchangeRequest result = getExchangeRequest(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNotNull();
        assertThat(result.getWithdrawalCancelId()).isNull();
        assertThat(result.getDepositId()).isNotNull();

        assertThat(logs).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_SUCCEEDED
        );
    }

    // 1.2. 입금 실패
    @Test
    @DisplayName("출금 성공 → 입금 실패 → 출금 취소 → 환전 실패")
    void 출금_성공_입금_실패_즉시_환전실패() {
        // Given
        // 1. 출금 성공
        requestWithdrawalPort.setSuccess(
                command.exchangeId(), "SUCCESS", "withdrawalId");

        // 2. 입금 실패
        requestDepositPort.setFailed(command.exchangeId(), "FAILED");

        // 3. 출금 취소
        cancelWithdrawalPort.setNormal(command.exchangeId(), completeWithdrawalCancel);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);
        RequestId requestId = getRequestId();
        ExchangeRequest result = getExchangeRequest(requestId);
        List<ExchangeState> states = getExchangeStates(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNotNull();
        assertThat(result.getWithdrawalCancelId()).isNotNull();
        assertThat(result.getDepositId()).isNull();

        assertThat(states).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CANCELING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 실패 확인 → 출금 취소 → 환전 실패")
    void 입금_실패_확인_후_출금_취소_플로우() {
        // Given
        // 1. 출금 성공
        requestWithdrawalPort.setSuccess(
                command.exchangeId(), "SUCCESS", "withdrawalId");

        // 2. 입금 결과 모름
        requestDepositPort.setThrowException(command.exchangeId());

        // 3. 입금 실패 확인
        loadDepositResultPort.setFailed(command.exchangeId(), "FAILED");

        // 4. 출금 취소
        cancelWithdrawalPort.setNormal(command.exchangeId(), completeWithdrawalCancel);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);
        RequestId requestId = getRequestId();
        List<ExchangeState> logs = getExchangeStates(requestId);
        ExchangeRequest result = getExchangeRequest(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNotNull();
        assertThat(result.getWithdrawalCancelId()).isNotNull();
        assertThat(result.getDepositId()).isNull();

        assertThat(logs).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED,
                ExchangeState.CANCELING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 결과 확인 실패(1회) → 입금 실패 확인 → 출금 취소 → 환전 실패")
    void 입금_결과_확인_실패_1회_후_입금_실패_확인_후_출금_취소_플로우() {
        // Given
        // 1. 출금 성공
        requestWithdrawalPort.setSuccess(
                command.exchangeId(), "SUCCESS", "withdrawalId");

        // 2. 입금 결과 모름
        requestDepositPort.setThrowException(command.exchangeId());

        // 3. 입금 결과 확인 실패 1회
        loadDepositResultPort.setThrowException(command.exchangeId());

        // 4. 입금 결과 조회 지연
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkDepositWithDelay.accept(1));

        // 5. 입금 실패 확인
        loadDepositResultPort.setFailed(command.exchangeId(), "FAILED");

        // 6. 출금 취소
        cancelWithdrawalPort.setNormal(command.exchangeId(), completeWithdrawalCancel);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);
        RequestId requestId = getRequestId();
        List<ExchangeState> logs = getExchangeStates(requestId);
        ExchangeRequest result = getExchangeRequest(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNotNull();
        assertThat(result.getWithdrawalCancelId()).isNotNull();
        assertThat(result.getDepositId()).isNull();

        assertThat(logs).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED,
                ExchangeState.CANCELING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 결과 확인 실패(2회) → 입금 실패 확인 → 출금 취소 → 환전 실패")
    void 입금_결과_확인_실패_2회_후_입금_실패_확인_후_출금_취소_플로우() {
        // Given
        // 1. 출금 성공
        requestWithdrawalPort.setSuccess(
                command.exchangeId(), "SUCCESS", "withdrawalId");

        // 2. 입금 결과 모름
        requestDepositPort.setThrowException(command.exchangeId());

        // 3. 입금 결과 확인 실패 2회
        loadDepositResultPort.setThrowException(command.exchangeId());
        loadDepositResultPort.setThrowException(command.exchangeId());

        // 4. 입금 결과 조회 지연 2회
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkDepositWithDelay.accept(1));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkDepositWithDelay.accept(2));

        // 5. 입금 실패 확인
        loadDepositResultPort.setFailed(command.exchangeId(), "FAILED");

        // 6. 출금 취소
        cancelWithdrawalPort.setNormal(command.exchangeId(), completeWithdrawalCancel);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);
        RequestId requestId = getRequestId();
        List<ExchangeState> logs = getExchangeStates(requestId);
        ExchangeRequest result = getExchangeRequest(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNotNull();
        assertThat(result.getWithdrawalCancelId()).isNotNull();
        assertThat(result.getDepositId()).isNull();

        assertThat(logs).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED,
                ExchangeState.CANCELING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 결과 확인 실패(3회) → 입금 실패 확인 → 출금 취소 → 환전 실패")
    void 입금_결과_확인_실패_3회_후_입금_실패_확인_후_출금_취소_플로우() {
        // Given
        // 1. 출금 성공
        requestWithdrawalPort.setSuccess(
                command.exchangeId(), "SUCCESS", "withdrawalId");

        // 2. 입금 결과 모름
        requestDepositPort.setThrowException(command.exchangeId());

        // 3. 입금 결과 확인 실패 3회
        loadDepositResultPort.setThrowException(command.exchangeId());
        loadDepositResultPort.setThrowException(command.exchangeId());
        loadDepositResultPort.setThrowException(command.exchangeId());

        // 4. 입금 결과 조회 지연 3회
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkDepositWithDelay.accept(1));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkDepositWithDelay.accept(2));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkDepositWithDelay.accept(3));

        // 5. 입금 실패 확인
        loadDepositResultPort.setFailed(command.exchangeId(), "FAILED");

        // 6. 출금 취소
        cancelWithdrawalPort.setNormal(command.exchangeId(), completeWithdrawalCancel);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);
        RequestId requestId = getRequestId();
        List<ExchangeState> logs = getExchangeStates(requestId);
        ExchangeRequest result = getExchangeRequest(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNotNull();
        assertThat(result.getWithdrawalCancelId()).isNotNull();
        assertThat(result.getDepositId()).isNull();

        assertThat(logs).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED,
                ExchangeState.CANCELING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 결과 모름 → 출금 성공 확인 → 출금 취소 → 환전 실패")
    void 출금_성공_확인_후_출금_취소_플로우() {
        // Given
        // 1. 출금 결과 모름
        requestWithdrawalPort.setThrowException(command.exchangeId());

        // 2. 출금 성공 확인
        loadWithdrawalResultPort.setSuccess(
                command.exchangeId(), "SUCCESS", "withdrawalId");

        // 3. 출금 취소
        cancelWithdrawalPort.setNormal(command.exchangeId(), completeWithdrawalCancel);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);
        RequestId requestId = getRequestId();
        ExchangeRequest result = getExchangeRequest(requestId);
        List<ExchangeState> states = getExchangeStates(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNotNull();
        assertThat(result.getWithdrawalCancelId()).isNotNull();
        assertThat(result.getDepositId()).isNull();

        assertThat(states).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED,
                ExchangeState.CANCELING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 결과 모름 → 출금 결과 확인 실패(1회) → 출금 성공 확인 → 출금 취소 → 환전 실패")
    void 출금_결과_확인_실패_1회_후_출금_성공_확인_후_출금_취소_플로우() {
        // Given
        // 1. 출금 결과 모름
        requestWithdrawalPort.setThrowException(command.exchangeId());

        // 2. 출금 결과 확인 실패 1회
        loadWithdrawalResultPort.setThrowException(command.exchangeId());

        // 3. 출금 결과 조회 지연
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkWithdrawalWithDelay.accept(1));

        // 4. 출금 성공 확인
        loadWithdrawalResultPort.setSuccess(command.exchangeId(), "SUCCESS", "withdrawalId");

        // 5. 출금 취소
        cancelWithdrawalPort.setNormal(command.exchangeId(), completeWithdrawalCancel);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);
        RequestId requestId = getRequestId();
        ExchangeRequest result = getExchangeRequest(requestId);
        List<ExchangeState> states = getExchangeStates(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNotNull();
        assertThat(result.getWithdrawalCancelId()).isNotNull();
        assertThat(result.getDepositId()).isNull();

        assertThat(states).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED,
                ExchangeState.CANCELING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 결과 모름 → 출금 결과 확인 실패(2회) → 출금 성공 확인 → 출금 취소 → 환전 실패")
    void 출금_결과_확인_실패_2회_후_출금_성공_확인_후_출금_취소_플로우() {
        // Given
        // 1. 출금 결과 모름
        requestWithdrawalPort.setThrowException(command.exchangeId());

        // 2. 출금 결과 확인 실패 2회
        loadWithdrawalResultPort.setThrowException(command.exchangeId());
        loadWithdrawalResultPort.setThrowException(command.exchangeId());

        // 3. 출금 결과 조회 지연 2회
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkWithdrawalWithDelay.accept(1));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkWithdrawalWithDelay.accept(2));

        // 4. 출금 성공 확인
        loadWithdrawalResultPort.setSuccess(command.exchangeId(), "SUCCESS", "withdrawalId");

        // 5. 출금 취소
        cancelWithdrawalPort.setNormal(command.exchangeId(), completeWithdrawalCancel);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);
        RequestId requestId = getRequestId();
        ExchangeRequest result = getExchangeRequest(requestId);
        List<ExchangeState> states = getExchangeStates(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNotNull();
        assertThat(result.getWithdrawalCancelId()).isNotNull();
        assertThat(result.getDepositId()).isNull();

        assertThat(states).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED,
                ExchangeState.CANCELING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 결과 모름 → 출금 결과 확인 실패(3회) → 출금 성공 확인 → 출금 취소 → 환전 실패")
    void 출금_결과_확인_실패_3회_후_출금_성공_확인_후_출금_취소_플로우() {
        // Given
        // 1. 출금 결과 모름
        requestWithdrawalPort.setThrowException(command.exchangeId());

        // 2. 출금 결과 확인 실패 3회
        loadWithdrawalResultPort.setThrowException(command.exchangeId());
        loadWithdrawalResultPort.setThrowException(command.exchangeId());
        loadWithdrawalResultPort.setThrowException(command.exchangeId());

        // 3. 출금 결과 조회 지연
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkWithdrawalWithDelay.accept(1));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkWithdrawalWithDelay.accept(2));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkWithdrawalWithDelay.accept(3));

        // 4. 출금 성공 확인
        loadWithdrawalResultPort.setSuccess(command.exchangeId(), "SUCCESS", "withdrawalId");

        // 5. 출금 취소
        cancelWithdrawalPort.setNormal(command.exchangeId(), completeWithdrawalCancel);

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);
        RequestId requestId = getRequestId();
        ExchangeRequest result = getExchangeRequest(requestId);
        List<ExchangeState> states = getExchangeStates(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNotNull();
        assertThat(result.getWithdrawalCancelId()).isNotNull();
        assertThat(result.getDepositId()).isNull();

        assertThat(states).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED,
                ExchangeState.CANCELING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    // 2. 출금 실패
    @Test
    @DisplayName("출금 실패 → 환전 실패")
    void 출금_즉시_실패_플로우() {
        // Given
        // 1. 출금 실패
        requestWithdrawalPort.setFailed(command.exchangeId(), "FAILED");

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);
        RequestId requestId = getRequestId();
        ExchangeRequest result = getExchangeRequest(requestId);
        List<ExchangeState> states = getExchangeStates(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNull();
        assertThat(result.getWithdrawalCancelId()).isNull();
        assertThat(result.getDepositId()).isNull();

        assertThat(states).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 결과 모름 → 출금 실패 확인 → 환전 실패")
    void 출금_실패_확인_후_환전_실패() {
        // Given
        // 1. 출금 결과 모름
        requestWithdrawalPort.setThrowException(command.exchangeId());

        // 2. 출금 실패 확인
        loadWithdrawalResultPort.setFailed(command.exchangeId(), "FAILED");

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);
        RequestId requestId = getRequestId();
        ExchangeRequest result = getExchangeRequest(requestId);
        List<ExchangeState> states = getExchangeStates(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNull();
        assertThat(result.getWithdrawalCancelId()).isNull();
        assertThat(result.getDepositId()).isNull();

        assertThat(states).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 결과 모름 → 출금 결과 확인 실패(1회) → 출금 실패 확인 → 환전 실패")
    void 출금_결과_확인_실패_1회_후_출금_실패_확인_후_환전_실패() {
        // Given
        // 1. 출금 결과 모름
        requestWithdrawalPort.setThrowException(command.exchangeId());

        // 2. 출금 결과 확인 실패 1회
        loadWithdrawalResultPort.setThrowException(command.exchangeId());

        // 3. 출금 결과 조회 지연
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkWithdrawalWithDelay.accept(1));

        // 4. 출금 실패 확인
        loadWithdrawalResultPort.setFailed(command.exchangeId(), "FAILED");

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);
        RequestId requestId = getRequestId();
        ExchangeRequest result = getExchangeRequest(requestId);
        List<ExchangeState> states = getExchangeStates(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNull();
        assertThat(result.getWithdrawalCancelId()).isNull();
        assertThat(result.getDepositId()).isNull();

        assertThat(states).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 결과 모름 → 출금 결과 확인 실패(2회) → 출금 실패 확인 → 환전 실패")
    void 출금_결과_확인_실패_2회_후_출금_실패_확인_후_환전_실패() {
        // Given
        // 1. 출금 결과 모름
        requestWithdrawalPort.setThrowException(command.exchangeId());

        // 2. 출금 결과 확인 실패 2회
        loadWithdrawalResultPort.setThrowException(command.exchangeId());
        loadWithdrawalResultPort.setThrowException(command.exchangeId());

        // 3. 출금 결과 조회 지연
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkWithdrawalWithDelay.accept(1));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkWithdrawalWithDelay.accept(2));

        // 4. 출금 실패 확인
        loadWithdrawalResultPort.setFailed(command.exchangeId(), "FAILED");

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);
        RequestId requestId = getRequestId();
        ExchangeRequest result = getExchangeRequest(requestId);
        List<ExchangeState> states = getExchangeStates(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNull();
        assertThat(result.getWithdrawalCancelId()).isNull();
        assertThat(result.getDepositId()).isNull();

        assertThat(states).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 결과 모름 → 출금 결과 확인 실패(3회) → 출금 실패 확인 → 환전 실패")
    void 출금_결과_확인_실패_3회_후_출금_실패_확인_후_환전_실패() {
        // Given
        // 1. 출금 결과 모름
        requestWithdrawalPort.setThrowException(command.exchangeId());

        // 2. 출금 결과 확인 실패 3회
        loadWithdrawalResultPort.setThrowException(command.exchangeId());
        loadWithdrawalResultPort.setThrowException(command.exchangeId());
        loadWithdrawalResultPort.setThrowException(command.exchangeId());

        // 3. 출금 결과 조회 지연
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkWithdrawalWithDelay.accept(1));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkWithdrawalWithDelay.accept(2));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkWithdrawalWithDelay.accept(3));

        // 4. 출금 실패 확인
        loadWithdrawalResultPort.setFailed(command.exchangeId(), "FAILED");

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);
        RequestId requestId = getRequestId();
        ExchangeRequest result = getExchangeRequest(requestId);
        List<ExchangeState> states = getExchangeStates(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.getWithdrawalId()).isNull();
        assertThat(result.getWithdrawalCancelId()).isNull();
        assertThat(result.getDepositId()).isNull();

        assertThat(states).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED,
                ExchangeState.EXCHANGE_CURRENCY_FAILED
        );
    }

    @Test
    @DisplayName("출금 결과 모름 → 출금 결과 확인 실패(4회) → 경고 메시지 발송")
    void 출금_결과_확인_실패_4회_후_경고_메시지_발송() {
        // Given
        // 1. 출금 결과 모름
        requestWithdrawalPort.setThrowException(command.exchangeId());

        // 2. 출금 결과 확인 실패 4회
        loadWithdrawalResultPort.setThrowException(command.exchangeId());
        loadWithdrawalResultPort.setThrowException(command.exchangeId());
        loadWithdrawalResultPort.setThrowException(command.exchangeId());
        loadWithdrawalResultPort.setThrowException(command.exchangeId());

        // 3. 출금 결과 조회 지연 4회
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkWithdrawalWithDelay.accept(1));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkWithdrawalWithDelay.accept(2));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkWithdrawalWithDelay.accept(3));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkWithdrawalWithDelay.accept(4));

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);
        RequestId requestId = getRequestId();
        ExchangeRequest result = getExchangeRequest(requestId);
        List<ExchangeState> states = getExchangeStates(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isFalse();
        assertThat(result.getWithdrawalId()).isNull();
        assertThat(result.getWithdrawalCancelId()).isNull();
        assertThat(result.getDepositId()).isNull();

        assertThat(states).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.CHECKING_WITHDRAWAL_REQUIRED
        );
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 결과 확인 실패(4회) → 경고 메시지 발송")
    void 입금_결과_확인_실패_4회_후_경고_메시지_발송() {
        // Given
        // 1. 출금 성공
        requestWithdrawalPort.setSuccess(
                command.exchangeId(), "SUCCESS", "withdrawalId");

        // 2. 입금 결과 모름
        requestDepositPort.setThrowException(command.exchangeId());

        // 3. 입금 결과 확인 실패 4회
        loadDepositResultPort.setThrowException(command.exchangeId());
        loadDepositResultPort.setThrowException(command.exchangeId());
        loadDepositResultPort.setThrowException(command.exchangeId());
        loadDepositResultPort.setThrowException(command.exchangeId());

        // 4. 입금 결과 조회 지연 4회
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkDepositWithDelay.accept(1));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkDepositWithDelay.accept(2));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkDepositWithDelay.accept(3));
        scheduleCheckRequestPort.setNormal(command.exchangeId(), () -> checkDepositWithDelay.accept(4));
        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(command))
                .isInstanceOf(RuntimeException.class);

        // Then
        sleepForAsync(1000);

        RequestId requestId = getRequestId();
        List<ExchangeState> logs = getExchangeStates(requestId);
        ExchangeRequest result = getExchangeRequest(requestId);

        assertThat(result).isNotNull();
        assertThat(result.isFinished()).isFalse();
        assertThat(result.getWithdrawalId()).isNotNull();
        assertThat(result.getDepositId()).isNull();

        assertThat(logs).contains(
                ExchangeState.EXCHANGE_CURRENCY_STARTED,
                ExchangeState.WITHDRAWAL_SUCCEEDED,
                ExchangeState.CHECKING_DEPOSIT_REQUIRED
        );
    }

}