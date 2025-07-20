package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.exchange.application.port.in.CheckDepositWithDelayUseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckWithdrawalWithDelayUseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.CompleteWithdrawalCancelUseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.ExchangeCurrencyUseCase;
import com.boldfaced7.fxexchange.exchange.application.port.out.cache.DeleteExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.cache.LoadExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.cache.SaveExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.cancel.CancelWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.cancel.SaveWithdrawalCancelPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.deposit.LoadDepositPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.deposit.RequestDepositPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.deposit.SaveDepositPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.exchange.LoadExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.exchange.SaveExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.exchange.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.external.ScheduleCheckRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.external.SendWarningMessagePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.log.SaveExchangeStateLogPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.withdrawal.LoadWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.withdrawal.RequestWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.withdrawal.SaveWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.util.ApplicationTestSupport;
import com.boldfaced7.fxexchange.exchange.domain.model.Deposit;
import com.boldfaced7.fxexchange.exchange.domain.model.Withdrawal;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RetryPolicy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;
import org.mockito.stubbing.Stubber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import static com.boldfaced7.fxexchange.exchange.application.util.TestConstraints.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("application-test")
class ExchangeCurrencyServiceApplicationTest extends ApplicationTestSupport {

    // UseCase
    @Autowired ExchangeCurrencyUseCase exchangeCurrencyService;
    @Autowired CheckDepositWithDelayUseCase checkDepositWithDelayService;
    @Autowired CheckWithdrawalWithDelayUseCase checkWithdrawalWithDelayService;
    @Autowired CompleteWithdrawalCancelUseCase completeWithdrawalCancelService;

    // cache
    @MockitoBean DeleteExchangeRequestCachePort deleteExchangeRequestCachePort;
    @MockitoBean LoadExchangeRequestCachePort loadExchangeRequestCachePort;
    @MockitoBean SaveExchangeRequestCachePort saveExchangeRequestCachePort;

    // cancel
    @MockitoBean CancelWithdrawalPort cancelWithdrawalPort;
    @MockitoBean SaveWithdrawalCancelPort saveWithdrawalCancelPort;

    // deposit
    @MockitoBean LoadDepositPort loadDepositPort;
    @MockitoBean RequestDepositPort requestDepositPort;
    @MockitoBean SaveDepositPort saveDepositPort;

    // exchange
    @MockitoBean LoadExchangeRequestPort loadExchangeRequestPort;
    @MockitoBean UpdateExchangeRequestPort updateExchangeRequestPort;
    @MockitoBean SaveExchangeRequestPort saveExchangeRequestPort;
    @MockitoBean RetryPolicy retryPolicy;

    // external
    @MockitoBean SendWarningMessagePort sendWarningMessagePort;
    @MockitoBean ScheduleCheckRequestPort scheduleCheckRequestPort;

    // log
    @MockitoBean SaveExchangeStateLogPort saveExchangeStateLogPort;

    // withdrawal
    @MockitoBean LoadWithdrawalPort loadWithdrawalPort;
    @MockitoBean RequestWithdrawalPort requestWithdrawalPort;
    @MockitoBean SaveWithdrawalPort saveWithdrawalPort;

    @BeforeEach
    void setUp() {
        // 모든 테스트 포트 초기화
        clearInvocations(
                loadDepositPort, requestDepositPort,
                cancelWithdrawalPort, loadWithdrawalPort, requestWithdrawalPort,
                sendWarningMessagePort, scheduleCheckRequestPort, saveExchangeStateLogPort,
                updateExchangeRequestPort, loadExchangeRequestPort, saveExchangeRequestPort,
                deleteExchangeRequestCachePort, loadExchangeRequestCachePort, saveExchangeRequestCachePort
        );

        stubAop();

        // 도메인 포트 stubbing
        when(saveExchangeRequestPort.save(any()))
                .thenReturn(EXCHANGE_REQUEST);

        when(updateExchangeRequestPort.update(any()))
                .thenReturn(EXCHANGE_REQUEST);

        when(loadExchangeRequestPort.loadByRequestIdForUpdate(any()))
                .thenReturn(Optional.of(EXCHANGE_REQUEST));
    }

    @Test
    @DisplayName("출금 성공 → 입금 성공 → 환전 성공")
    void 출금_성공_입금_성공_환전_성공() {
        // Given
        stubWithdrawalRequest(Result.SUCCESS);                   // 1. 출금 성공 stubbing
        stubDepositRequest(Result.SUCCESS);                      // 2. 입금 성공 stubbing

        // When
        var result = exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND);

        // Then
        assertThat(result).isNotNull();

        verifySaga(() -> {
            verifyWithdrawalRequest(Result.SUCCESS);              // 1. 출금 성공 검증
            verifyDepositRequest(Result.SUCCESS);                 // 2. 입금 성공 검증
        });
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 성공 확인 → 환전 성공")
    void 입금_성공_확인_후_환전_성공() {
        // Given
        stubWithdrawalRequest(Result.SUCCESS);                   // 1. 출금 성공 stubbing
        stubDepositRequest(Result.UNKNOWN);                      // 2. 입금 결과 모름 stubbing
        stubDepositRequestCheck(0, Result.SUCCESS);              // 3. 입금 성공 확인 stubbing

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        verifySaga(() -> {
            verifyWithdrawalRequest(Result.SUCCESS);              // 1. 출금 성공 검증
            verifyDepositRequest(Result.UNKNOWN);                 // 2. 입금 결과 모름 검증
            verifyDepositRequestCheck(0, Result.SUCCESS);         // 3. 입금 성공 확인 검증
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("failureCountArguments")
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 결과 확인 실패(N회) → 입금 성공 확인 → 환전 성공")
    void 입금_결과_확인_실패_N회_후_환전_성공(String message, int count) {
        // Given
        stubWithdrawalRequest(Result.SUCCESS);                   // 1. 출금 성공 stubbing
        stubDepositRequest(Result.UNKNOWN);                      // 2. 입금 결과 모름 stubbing
        stubDepositRequestCheck(count, Result.SUCCESS);          // 3. 입금 결과 확인 실패 1회 stubbing
        stubDepositCheckRequestSchedule(count);                  // 4. 입금 성공 확인 stubbing

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        verifySaga(() -> {
            verifyWithdrawalRequest(Result.SUCCESS);              // 1. 출금 성공 검증
            verifyDepositRequest(Result.UNKNOWN);                 // 2. 입금 결과 모름 검증
            verifyDepositRequestCheck(count, Result.SUCCESS);     // 3. 입금 결과 확인 실패 1회 검증
            verifyDepositCheckRequestSchedule(count);             // 4. 입금 성공 확인 검증
        });
    }

    @Test
    @DisplayName("출금 성공 → 입금 실패 → 출금 취소 → 환전 실패")
    void 출금_성공_입금_실패_즉시_환전실패() {
        // Given
        stubWithdrawalRequest(Result.SUCCESS);                   // 1. 출금 성공 stubbing
        stubDepositRequest(Result.FAILURE);                      // 2. 입금 실패 stubbing
        stubWithdrawalCancel();                                  // 3. 출금 취소 stubbing

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        verifySaga(() -> {
            verifyWithdrawalRequest(Result.SUCCESS);              // 1. 출금 성공 검증
            verifyDepositRequest(Result.FAILURE);                 // 2. 입금 실패 검증
            verifyWithdrawalCancel();                             // 3. 출금 취소 검증
        });
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 실패 확인 → 출금 취소 → 환전 실패")
    void 입금_실패_확인_후_출금_취소_플로우() {
        // Given
        stubWithdrawalRequest(Result.SUCCESS);                   // 1. 출금 성공 stubbing
        stubDepositRequest(Result.UNKNOWN);                      // 2. 입금 결과 모름 stubbing
        stubDepositRequestCheck(0, Result.FAILURE);              // 3. 입금 실패 확인 stubbing
        stubWithdrawalCancel();                                  // 4. 출금 취소 stubbing

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        verifySaga(() -> {
            verifyWithdrawalRequest(Result.SUCCESS);              // 1. 출금 성공 검증
            verifyDepositRequest(Result.UNKNOWN);                 // 2. 입금 결과 모름 검증
            verifyDepositRequestCheck(0, Result.FAILURE);         // 3. 입금 실패 확인 검증
            verifyWithdrawalCancel();                             // 4. 출금 취소 검증
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("failureCountArguments")
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 결과 확인 실패(N회) → 입금 실패 확인 → 출금 취소 → 환전 실패")
    void 입금_결과_확인_실패_N회_후_입금_실패_확인_후_출금_취소_플로우(String message, int count) {
        // Given
        stubWithdrawalRequest(Result.SUCCESS);                   // 1. 출금 성공 stubbing
        stubDepositRequest(Result.UNKNOWN);                      // 2. 입금 결과 모름 stubbing
        stubDepositRequestCheck(count, Result.FAILURE);          // 3. 입금 결과 확인 실패 1회 stubbing
        stubDepositCheckRequestSchedule(count);                  // 4. 입금 실패 확인 stubbing
        stubWithdrawalCancel();                                  // 5. 출금 취소 stubbing

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        verifySaga(() -> {
            verifyWithdrawalRequest(Result.SUCCESS);              // 1. 출금 성공 검증
            verifyDepositRequest(Result.UNKNOWN);                 // 2. 입금 결과 모름 검증
            verifyDepositRequestCheck(count, Result.FAILURE);     // 3. 입금 결과 확인 실패 1회 검증
            verifyDepositCheckRequestSchedule(count);             // 4. 입금 실패 확인 1회 검증
            verifyWithdrawalCancel();                             // 5. 출금 취소 검증
        });
    }


    @Test
    @DisplayName("출금 결과 모름 → 출금 성공 확인 → 출금 취소 → 환전 실패")
    void 출금_성공_확인_후_출금_취소_플로우() {
        // Given
        stubWithdrawalRequest(Result.UNKNOWN);                   // 1. 출금 결과 모름 stubbing
        stubWithdrawalRequestCheck(0, Result.SUCCESS);           // 2. 출금 성공 확인 stubbing
        stubWithdrawalCancel();                                  // 3. 출금 취소 stubbing

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        verifySaga(() -> {
            verifyWithdrawalRequest(Result.UNKNOWN);              // 1. 출금 결과 모름 검증
            verifyWithdrawalRequestCheck(0, Result.SUCCESS);      // 2. 출금 성공 확인 검증
            verifyWithdrawalCancel();                             // 3. 출금 취소 검증
        });
    }

   @ParameterizedTest(name = "{0}")
   @MethodSource("failureCountArguments")
   @DisplayName("출금 결과 모름 → 출금 결과 확인 실패(N회) → 출금 성공 확인 → 출금 취소 → 환전 실패")
   void 출금_결과_확인_실패_N회_후_출금_성공_확인_후_출금_취소_플로우(String message, int count) {
       // Given
       stubWithdrawalRequest(Result.UNKNOWN);                   // 1. 출금 결과 모름 stubbing
       stubWithdrawalRequestCheck(count, Result.SUCCESS);       // 2. 출금 결과 확인 실패 1회 stubbing
       stubWithdrawalCheckRequestSchedule(count);               // 3. 출금 성공 확인 stubbing
       stubWithdrawalCancel();                                  // 4. 출금 취소 stubbing

       // When
       assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
               .isInstanceOf(RuntimeException.class);

       // Then
       verifySaga(() -> {
           verifyWithdrawalRequest(Result.UNKNOWN);              // 1. 출금 결과 모름 검증
           verifyWithdrawalRequestCheck(count, Result.SUCCESS);  // 2. 출금 결과 확인 실패 1회 검증
           verifyWithdrawalCheckRequestSchedule(count);          // 3. 출금 결과 조회 지연 검증
           verifyWithdrawalCancel();                             // 4. 출금 취소 검증
       });
   }

    @Test
    @DisplayName("출금 결과 모름 → 출금 결과 확인 실패(4회) → 경고 메시지 발송")
    void 출금_결과_확인_실패_4회_후_경고_메시지_발송() {
        // Given
        stubWithdrawalRequest(Result.UNKNOWN);                   // 1. 출금 결과 모름 stubbing
        stubWithdrawalRequestCheck(4, Result.UNKNOWN);           // 2. 출금 결과 확인 실패 4회 stubbing
        stubWithdrawalCheckRequestSchedule(3);                   // 3. 출금 결과 조회 지연 stubbing
        stubSendWarningMessage();                                // 4. 경고 메시지 발송 stubbing

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        verifySaga(() -> {
            verifyWithdrawalRequest(Result.UNKNOWN);              // 1. 출금 결과 모름 검증
            verifyWithdrawalRequestCheck(4, Result.UNKNOWN);      // 2. 출금 결과 확인 실패 4회 검증
            verifyWithdrawalCheckRequestSchedule(3);              // 3. 출금 결과 조회 지연 3회 검증
            verifySendWarningMessage();                           // 4. 경고 메시지 발송 검증
        });
    }

    @Test
    @DisplayName("출금 성공 → 입금 결과 모름 → 입금 결과 확인 실패(4회) → 경고 메시지 발송")
    void 입금_결과_확인_실패_4회_후_경고_메시지_발송() {
        // Given
        stubWithdrawalRequest(Result.SUCCESS);                   // 1. 출금 성공 stubbing
        stubDepositRequest(Result.UNKNOWN);                      // 2. 입금 결과 모름 stubbing
        stubDepositRequestCheck(4, Result.UNKNOWN);              // 3. 입금 결과 확인 실패 4회 stubbing
        stubDepositCheckRequestSchedule(3);                      // 4. 입금 결과 조회 지연 stubbing
        stubSendWarningMessage();                                // 5. 경고 메시지 발송 stubbing

        // When
        assertThatThrownBy(() -> exchangeCurrencyService.exchangeCurrency(EXCHANGE_CURRENCY_COMMAND))
                .isInstanceOf(RuntimeException.class);

        // Then
        verifySaga(() -> {
            verifyWithdrawalRequest(Result.SUCCESS);              // 1. 출금 성공 검증
            verifyDepositRequest(Result.UNKNOWN);                 // 2. 입금 결과 모름 검증
            verifyDepositRequestCheck(4, Result.UNKNOWN);         // 3. 입금 결과 확인 실패 4회 검증
            verifyDepositCheckRequestSchedule(3);                 // 4. 입금 결과 조회 지연 3회 검증
            verifySendWarningMessage();                           // 5. 경고 메시지 발송 검증
        });
    }

    void stubWithdrawalRequest(Result result) {
        OngoingStubbing<Withdrawal> when = when(requestWithdrawalPort.withdraw(any()));

        switch (result) {
            case SUCCESS -> when.thenReturn(withdrawal(true, true));
            case FAILURE -> when.thenReturn(withdrawal(true, false));
            case UNKNOWN -> when.thenThrow(new RuntimeException("Unknown withdrawal result"));
        }
    }

    void verifyWithdrawalRequest(Result result) {
        verify(requestWithdrawalPort).withdraw(any());

        if (result.equals(Result.UNKNOWN)) {
            verify(saveExchangeRequestCachePort).save(any());
        } else if (result.equals(Result.SUCCESS)) {
            verify(saveWithdrawalPort).saveWithdrawal(any());
        }
    }


    void stubDepositRequest(Result result) {
        OngoingStubbing<Deposit> when = when(requestDepositPort.deposit(any()));

        switch (result) {
            case SUCCESS -> when.thenReturn(deposit(true, true));
            case FAILURE -> when.thenReturn(deposit(true, false));
            case UNKNOWN -> when.thenThrow(new RuntimeException("Unknown deposit result"));
        }
    }

    void verifyDepositRequest(Result result) {
        verify(requestDepositPort).deposit(any());

        if (result.equals(Result.UNKNOWN)) {
            verify(saveExchangeRequestCachePort).save(any());
        } else if (result.equals(Result.SUCCESS)) {
            verify(saveDepositPort).saveDeposit(any());
        }
    }


    void stubWithdrawalRequestCheck(int unknownCount, Result result) {
        when(loadExchangeRequestCachePort.loadByRequestId(any()))
                .thenReturn(Optional.of(EXCHANGE_REQUEST));

        OngoingStubbing<Withdrawal> when = when(loadWithdrawalPort.loadWithdrawal(any()));

        for (int i = 0; i < unknownCount; i++) {
            when = when.thenThrow(new RuntimeException("Unknown withdrawal result"));
        }
        switch (result) {
            case SUCCESS -> when.thenReturn(withdrawal(false, true));
            case FAILURE -> when.thenReturn(withdrawal(false, false));
            case UNKNOWN -> when.thenThrow(new RuntimeException("Unknown withdrawal result"));
        }

        if (unknownCount > 0 || result.equals(Result.UNKNOWN)) {
            stubLoadExchangeProperties();
        }
    }

    void verifyWithdrawalRequestCheck(int unknownCount, Result result) {
        verify(loadExchangeRequestCachePort, atLeast(unknownCount)).loadByRequestId(any());
        verify(loadWithdrawalPort, atLeast(unknownCount)).loadWithdrawal(any());

        if (!result.equals(Result.UNKNOWN)) {
            verify(saveWithdrawalPort).saveWithdrawal(any());
        }
    }


    void stubDepositRequestCheck(int unknownCount, Result result) {
        when(loadExchangeRequestCachePort.loadByRequestId(any()))
                .thenReturn(Optional.of(EXCHANGE_REQUEST));

        OngoingStubbing<Deposit> when = when(loadDepositPort.loadDeposit(any()));

        for (int i = 0; i < unknownCount; i++) {
            when = when.thenThrow(new RuntimeException("Unknown deposit result"));
        }
        switch (result) {
            case SUCCESS -> when.thenReturn(deposit(false, true));
            case FAILURE -> when.thenReturn(deposit(false, false));
            case UNKNOWN -> when.thenThrow(new RuntimeException("Unknown deposit result"));
        }

        if (unknownCount > 0 || result.equals(Result.UNKNOWN)) {
            stubLoadExchangeProperties();
        }

    }

    void verifyDepositRequestCheck(int unknownCount, Result result) {
        verify(loadExchangeRequestCachePort, atLeast(unknownCount)).loadByRequestId(any());
        verify(loadDepositPort, atLeast(unknownCount)).loadDeposit(any());

        if (!result.equals(Result.UNKNOWN)) {
            verify(saveWithdrawalPort).saveWithdrawal(any());
        }

    }

    void stubWithdrawalCheckRequestSchedule(int count) {
        Stubber doAnswer = doAnswer(answerCheckWithdrawalWithDelay(1));

        for (int i = 2; i <= count; i++) {
            doAnswer = doAnswer.doAnswer(answerCheckWithdrawalWithDelay(i));
        }
        doAnswer.when(scheduleCheckRequestPort)
                .scheduleCheckRequest(any(), any(), any(), any(), any());

        when(loadExchangeRequestCachePort.loadByExchangeId(any()))
                .thenReturn(Optional.of(EXCHANGE_REQUEST));
    }

    Answer<?> answerCheckWithdrawalWithDelay(int count) {
        return invocation -> {
            log.info("checkWithdrawalWithDelay, count: {}", count);
            checkWithdrawalWithDelayService.checkWithdrawalWithDelay(
                    checkWithdrawalWithDelayCommand(count)
            );
            return null;
        };
    }

    void verifyWithdrawalCheckRequestSchedule(int count) {
        verify(scheduleCheckRequestPort, atLeast(count))
                .scheduleCheckRequest(any(), any(), any(), any(), any());
    }


    void stubDepositCheckRequestSchedule(int count) {
        Stubber doAnswer = doAnswer(answerCheckDepositWithDelay(1));

        for (int i = 2; i <= count; i++) {
            doAnswer = doAnswer.doAnswer(answerCheckDepositWithDelay(i));
        }
        doAnswer.when(scheduleCheckRequestPort)
                .scheduleCheckRequest(any(), any(), any(), any(), any());

        when(loadExchangeRequestCachePort.loadByExchangeId(any()))
                .thenReturn(Optional.of(EXCHANGE_REQUEST));
    }

    Answer<?> answerCheckDepositWithDelay(int count) {
        return invocation -> {
            log.info("checkCheckDepositWithDelay, count: {}", count);
            checkDepositWithDelayService.checkDepositWithDelay(
                    checkDepositWithDelayCommand(count)
            );
            return null;
        };
    }

    void verifyDepositCheckRequestSchedule(int count) {
        verify(scheduleCheckRequestPort, atLeast(count))
                .scheduleCheckRequest(any(), any(), any(), any(), any());
    }


    void stubWithdrawalCancel() {
        doAnswer(answerCompleteWithdrawalCancel())
                .when(cancelWithdrawalPort).cancelWithdrawal(any(), any());

        when(loadExchangeRequestCachePort.loadByExchangeId(any()))
                .thenReturn(Optional.of(EXCHANGE_REQUEST));
    }

    Answer<?> answerCompleteWithdrawalCancel() {
        return invocation -> {
            log.info("completeWithdrawalCancel");
            completeWithdrawalCancelService.completeWithdrawalCancel(
                    COMPLETE_WITHDRAWAL_CANCEL_COMMAND
            );
            return null;
        };
    }

    void verifyWithdrawalCancel() {
        verify(cancelWithdrawalPort).cancelWithdrawal(any(), any());
    }


    void stubSendWarningMessage() {
        doNothing().when(sendWarningMessagePort).sendWarningMessage(any(), any());
    }

    void verifySendWarningMessage() {
        verify(sendWarningMessagePort).sendWarningMessage(any(), any());
    }


    void stubLoadExchangeProperties() {
        when(retryPolicy.criteria()).thenReturn(new Count(3));
        when(retryPolicy.calculateDelay(any(Count.class)))
                .thenReturn(Duration.ofSeconds(1));
    }

    static Stream<Arguments> failureCountArguments() {
        return Stream.of(
                Arguments.of("1회", 1),
                Arguments.of("2회", 2),
                Arguments.of("3회", 3)
        );
    }

    enum Result { SUCCESS, FAILURE, UNKNOWN }
}