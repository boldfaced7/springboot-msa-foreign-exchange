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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
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

    @BeforeEach
    void setUp() {
        requestId = new RequestId(1L);
        successResult = new WithdrawalResult(true, new AccountCommandStatus("SUCCESS"), new WithdrawalId("withdrawal-123"));
        failureResult = new WithdrawalResult(false, new AccountCommandStatus("FAILED"), new WithdrawalId("withdrawal-456"));
    }

    @Test
    @DisplayName("출금 성공 확인 시, 입금 ID가 추가되어야 한다")
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
    @DisplayName("출금 실패 확인 시, exchangeRequest가 업데이트 되지 않아야 한다")
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
    @DisplayName("출금 결과 확인 실패 시, exchangeRequest가 업데이트 되지 않아야 한다")
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
    @DisplayName("출금 결과 조회 시 전달하는 람다를 검증한다")
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
        verify(exchangeRequest).withdrawalCheckUnknown(Count.zero().increase());
    }

}