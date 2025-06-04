package com.boldfaced7.fxexchange.exchange.application.service.withdrawal;

import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.ParamEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.SimpleEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater.ParamRequestUpdater;
import com.boldfaced7.fxexchange.exchange.application.service.withdrawal.impl.WithdrawServiceImpl;
import com.boldfaced7.fxexchange.exchange.application.service.withdrawal.impl.WithdrawalRequesterImpl;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class WithdrawServiceImplTest {

    @InjectMocks
    private WithdrawServiceImpl withdrawService;

    @Mock
    private WithdrawalRequesterImpl withdrawalRequester;

    @Mock
    private ExchangeRequestUpdater exchangeRequestUpdater;

    @Mock
    private ExchangeRequest exchangeRequest;

    private WithdrawalResult successResult;
    private WithdrawalResult failureResult;

    @Captor
    private ArgumentCaptor<SimpleEventPublisher> withdrawalSucceededCaptor;

    @Captor
    private ArgumentCaptor<SimpleEventPublisher> withdrawalFailedCaptor;

    @Captor
    private ArgumentCaptor<ParamEventPublisher<Count>> withdrawalResultUnknownCaptor;

    @BeforeEach
    void setUp() {
        successResult = new WithdrawalResult(true, new AccountCommandStatus("SUCCESS"), new WithdrawalId("withdrawal-123"));
        failureResult = new WithdrawalResult(false, new AccountCommandStatus("FAILED"), new WithdrawalId("withdrawal-456"));
    }

    @Test
    @DisplayName("출금 성공 시, 출금 ID가 추가되어야 한다")
    void withdraw_Success() {
        // given
        doReturn(successResult).when(withdrawalRequester).requestWithdrawal(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class)
        );
        doReturn(exchangeRequest).when(exchangeRequestUpdater).update(
                eq(exchangeRequest),
                any(ParamRequestUpdater.class),
                eq(successResult.withdrawalId())
        );

        // when
        WithdrawalDetail result = withdrawService.withdraw(exchangeRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.exchangeRequest()).isEqualTo(exchangeRequest);
        assertThat(result.withdrawalResult()).isEqualTo(successResult);

        verify(withdrawalRequester).requestWithdrawal(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class)
        );
        verify(exchangeRequestUpdater).update(
                eq(exchangeRequest),
                any(ParamRequestUpdater.class),
                eq(successResult.withdrawalId())
        );
    }

    @Test
    @DisplayName("출금 실패 시, 출금 ID가 추가되지 않아야 한다")
    void withdraw_Failure() {
        // given
        doReturn(failureResult).when(withdrawalRequester).requestWithdrawal(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class)
        );

        // when & then
        assertThatThrownBy(() -> withdrawService.withdraw(exchangeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Withdraw failed");

        verify(withdrawalRequester).requestWithdrawal(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class)
        );
        verify(exchangeRequestUpdater, never()).update(
                eq(exchangeRequest),
                any(ParamRequestUpdater.class),
                eq(failureResult.withdrawalId())
        );
    }

    @Test
    @DisplayName("출금 요청 중 예외 발생 시, 출금 ID가 추가되지 않아야 한다")
    void withdraw_Exception() {
        // given
        doThrow(new RuntimeException("Network error")).when(withdrawalRequester).requestWithdrawal(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class)
        );

        // when & then
        assertThatThrownBy(() -> withdrawService.withdraw(exchangeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Network error");

        verify(withdrawalRequester).requestWithdrawal(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class)
        );
        verify(exchangeRequestUpdater, never()).update(
                eq(exchangeRequest),
                any(ParamRequestUpdater.class),
                eq(failureResult.withdrawalId())
        );
    }

    @Test
    @DisplayName("출금 요청 시 전달되는 람다를 검증한다")
    void withdraw_LambdaVerification() {
        // given
        doReturn(successResult).when(withdrawalRequester).requestWithdrawal(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class)
        );

        // when
        withdrawService.withdraw(exchangeRequest);

        // then
        // 1. 람다식 캡처 확인
        verify(withdrawalRequester).requestWithdrawal(
                eq(exchangeRequest),
                withdrawalSucceededCaptor.capture(),
                withdrawalFailedCaptor.capture(),
                withdrawalResultUnknownCaptor.capture()
        );

        // 2. 각 람다식의 동작 검증
        // 2.1 성공 람다 검증
        withdrawalSucceededCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).withdrawalSucceeded();

        // 2.2 실패 람다 검증
        withdrawalFailedCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).withdrawalFailed();

        // 2.3 지연 체크 람다 검증
        withdrawalResultUnknownCaptor.getValue().publish(exchangeRequest, Count.zero().increase());
        verify(exchangeRequest).withdrawalResultUnknown(Count.zero().increase());
    }
}