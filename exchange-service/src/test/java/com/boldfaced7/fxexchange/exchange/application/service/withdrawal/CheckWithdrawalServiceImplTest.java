package com.boldfaced7.fxexchange.exchange.application.service.withdrawal;

import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.ParamEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.SimpleEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater.ParamRequestUpdater;
import com.boldfaced7.fxexchange.exchange.application.service.withdrawal.impl.CheckWithdrawalServiceImpl;
import com.boldfaced7.fxexchange.exchange.application.service.withdrawal.impl.WithdrawalResultCheckerImpl;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
    "exchange.withdrawal.check.max-count=3"
})
class CheckWithdrawalServiceImplTest {

    @InjectMocks
    private CheckWithdrawalServiceImpl checkWithdrawalService;

    @Mock
    private ExchangeRequestLoader exchangeRequestLoader;

    @Mock
    private WithdrawalResultCheckerImpl withdrawalResultChecker;

    @Mock
    private ExchangeRequestUpdater exchangeRequestUpdater;

    @Mock
    private ExchangeRequest exchangeRequest;

    private RequestId requestId;
    private WithdrawalResult successResult;
    private WithdrawalResult failureResult;

    @Captor
    private ArgumentCaptor<SimpleEventPublisher> successCheckedCaptor;

    @Captor
    private ArgumentCaptor<SimpleEventPublisher> failureCheckedCaptor;

    @Captor
    private ArgumentCaptor<ParamEventPublisher<Count>> delayingCheckRequiredCaptor;

    @Captor
    private ArgumentCaptor<ParamEventPublisher<Count>> sendingWarningMessageRequiredCaptor;

    @BeforeEach
    void setUp() {
        requestId = new RequestId(1L);
        successResult = new WithdrawalResult(true, new AccountCommandStatus("SUCCESS"), new WithdrawalId("withdrawal-123"));
        failureResult = new WithdrawalResult(false, new AccountCommandStatus("FAILED"), new WithdrawalId("withdrawal-456"));
    }

    @Test
    @DisplayName("출금 결과 조회 성공 시, 출금 ID가 추가되어야 한다")
    void checkWithdrawal_Success() {
        // given
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        doReturn(successResult).when(withdrawalResultChecker).loadWithdrawalResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(Count.zero().increase())
        );

        // when
        checkWithdrawalService.checkWithdrawal(requestId, Count.zero(), Direction.BUY);

        // then
        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(withdrawalResultChecker).loadWithdrawalResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(Count.zero().increase())
        );
        verify(exchangeRequestUpdater).update(
                eq(exchangeRequest),
                any(ParamRequestUpdater.class),
                eq(successResult.withdrawalId())
        );
    }

    @Test
    @DisplayName("출금 결과 조회 실패 시, 출금 ID가 추가되지 않아야 한다")
    void checkWithdrawal_Failure() {
        // given
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        doReturn(failureResult).when(withdrawalResultChecker).loadWithdrawalResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(Count.zero().increase())
        );

        // when
        checkWithdrawalService.checkWithdrawal(requestId, Count.zero(), Direction.BUY);

        // then
        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(withdrawalResultChecker).loadWithdrawalResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(Count.zero().increase())
        );
        verify(exchangeRequestUpdater, never()).update(
                eq(exchangeRequest),
                any(ParamRequestUpdater.class),
                eq(failureResult.withdrawalId())
        );
    }

    @Test
    @DisplayName("출금 결과 조회 실패 시 재시도 횟수가 최대값보다 작으면 지연 체크가 필요하다")
    void checkWithdrawal_Retry() {
        // given
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        doThrow(new RuntimeException("Network error")).when(withdrawalResultChecker).loadWithdrawalResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(Count.zero().increase())
        );

        // when & then
        assertThatThrownBy(() -> checkWithdrawalService.checkWithdrawal(requestId, Count.zero(), Direction.BUY))
                .isInstanceOf(RuntimeException.class);

        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(withdrawalResultChecker).loadWithdrawalResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(Count.zero().increase())
        );
        verify(exchangeRequestUpdater, never()).update(
                eq(exchangeRequest),
                any(ParamRequestUpdater.class),
                eq(failureResult.withdrawalId())
        );
    }

    @Test
    @DisplayName("출금 결과 조회 재시도 횟수 초과 시, 경고 메시지 발송이 필요하다")
    void checkWithdrawal_RetryExceeded() {
        // given
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        doThrow(new RuntimeException("Network error")).when(withdrawalResultChecker).loadWithdrawalResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(new Count(4))
        );

        // when & then
        assertThatThrownBy(() -> checkWithdrawalService.checkWithdrawal(requestId, new Count(3), Direction.BUY))
                .isInstanceOf(RuntimeException.class);

        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(withdrawalResultChecker).loadWithdrawalResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(new Count(4))
        );
        verify(exchangeRequestUpdater, never()).update(
                eq(exchangeRequest),
                any(ParamRequestUpdater.class),
                eq(failureResult.withdrawalId())
        );
    }

    @Test
    @DisplayName("출금 결과 조회 시 count가 0인 경우 전달하는 람다를 검증한다")
    void checkWithdrawal_LambdaVerification_WhenCountIsZero() {
        // given
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        doReturn(successResult).when(withdrawalResultChecker).loadWithdrawalResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(Count.zero().increase())
        );
        // when
        checkWithdrawalService.checkWithdrawal(requestId, Count.zero(), Direction.BUY);

        // then
        // 1. 람다식 캡처 확인
        verify(withdrawalResultChecker).loadWithdrawalResult(
                eq(exchangeRequest),
                successCheckedCaptor.capture(),
                failureCheckedCaptor.capture(),
                delayingCheckRequiredCaptor.capture(),
                eq(Count.zero().increase())
        );
        // 2. 각 람다식의 동작 검증
        // 2.1 성공 람다 검증
        successCheckedCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).withdrawalSuccessChecked();

        // 2.2 실패 람다 검증
        failureCheckedCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).withdrawalFailureChecked();

        // 2.3 지연 체크 람다 검증
        delayingCheckRequiredCaptor.getValue().publish(exchangeRequest, Count.zero().increase());
        verify(exchangeRequest).delayingWithdrawalCheckRequired(Count.zero().increase());
    }

    @Test
    @DisplayName("출금 결과 조회 시 count가 최댓값을 초과한 경우 전달하는 람다를 검증한다")
    void checkWithdrawal_LambdaVerification_WhenCountExceedsMax() {
        // given
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        doReturn(successResult).when(withdrawalResultChecker).loadWithdrawalResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(new Count(4))
        );
        // when
        checkWithdrawalService.checkWithdrawal(requestId, new Count(3), Direction.BUY);

        // then
        // 1. 람다식 캡처 확인
        verify(withdrawalResultChecker).loadWithdrawalResult(
                eq(exchangeRequest),
                successCheckedCaptor.capture(),
                failureCheckedCaptor.capture(),
                sendingWarningMessageRequiredCaptor.capture(),
                eq(new Count(4))
        );

        // 2. 각 람다식의 동작 검증
        // 2.1 성공 람다 검증
        successCheckedCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).withdrawalSuccessChecked();

        // 2.2 실패 람다 검증
        failureCheckedCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).withdrawalFailureChecked();

        // 2.3 경고 메시지 발송 람다 검증
        sendingWarningMessageRequiredCaptor.getValue().publish(exchangeRequest, new Count(4));
        verify(exchangeRequest).sendingWarningMessageRequired(new Count(4));
    }
}