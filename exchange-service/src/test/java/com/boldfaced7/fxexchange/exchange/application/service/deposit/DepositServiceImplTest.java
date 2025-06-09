package com.boldfaced7.fxexchange.exchange.application.service.deposit;

import com.boldfaced7.fxexchange.exchange.application.service.deposit.impl.DepositRequesterImpl;
import com.boldfaced7.fxexchange.exchange.application.service.deposit.impl.DepositServiceImpl;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.ParamEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.SimpleEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater.ParamRequestUpdater;
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
class DepositServiceImplTest {

    @InjectMocks
    private DepositServiceImpl depositService;

    @Mock
    private DepositRequesterImpl depositRequester;

    @Mock
    private ExchangeRequestUpdater exchangeRequestUpdater;

    @Mock
    private ExchangeRequest exchangeRequest;

    private DepositResult successResult;
    private DepositResult failureResult;

    @Captor
    private ArgumentCaptor<SimpleEventPublisher> depositSucceededCaptor;

    @Captor
    private ArgumentCaptor<SimpleEventPublisher> depositFailedCaptor;

    @Captor
    private ArgumentCaptor<ParamEventPublisher<Count>> depositResultUnknownCaptor;

    @BeforeEach
    void setUp() {
        successResult = new DepositResult(
                true,
                new AccountCommandStatus("SUCCESS"),
                new DepositId("deposit-123")
        );
        failureResult = new DepositResult(
                false,
                new AccountCommandStatus("FAILED"),
                new DepositId("deposit-456")
        );
    }

    @Test
    @DisplayName("입금 성공 시, 입금 ID가 추가되어야 한다")
    void deposit_Success() {
        // given
        doReturn(successResult).when(depositRequester).requestDeposit(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class)
        );
        doReturn(exchangeRequest).when(exchangeRequestUpdater).update(
                eq(exchangeRequest),
                any(ParamRequestUpdater.class),
                eq(successResult.depositId())
        );

        // when
        DepositDetail result = depositService.deposit(exchangeRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.exchangeRequest()).isEqualTo(exchangeRequest);
        assertThat(result.depositResult()).isEqualTo(successResult);

        verify(depositRequester).requestDeposit(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class)
        );
        verify(exchangeRequestUpdater).update(
                eq(exchangeRequest),
                any(ParamRequestUpdater.class),
                eq(successResult.depositId())
        );
    }

    @Test
    @DisplayName("입금 실패 시, 입금 ID가 추가되지 않아야 한다")
    void deposit_Failure() {
        // given
        doReturn(failureResult).when(depositRequester).requestDeposit(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class)
        );

        // when & then
        assertThatThrownBy(() -> depositService.deposit(exchangeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Deposit failed");

        verify(depositRequester).requestDeposit(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class)
        );
        verify(exchangeRequestUpdater, never()).update(
                eq(exchangeRequest),
                any(ParamRequestUpdater.class),
                eq(failureResult.depositId())
        );
    }

    @Test
    @DisplayName("입금 요청 중 예외 발생 시, 입금 ID가 추가되지 않아야 한다")
    void deposit_Exception() {
        // given
        doThrow(new RuntimeException("Network error")).when(depositRequester).requestDeposit(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class)
        );

        // when & then
        assertThatThrownBy(() -> depositService.deposit(exchangeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Network error");

        verify(depositRequester).requestDeposit(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class)
        );
        verify(exchangeRequestUpdater, never()).update(
                eq(exchangeRequest),
                any(ParamRequestUpdater.class),
                eq(failureResult.depositId())
        );
    }

    @Test
    @DisplayName("입금 요청 시 전달되는 람다를 검증한다")
    void deposit_LambdaVerification() {
        // given
        doReturn(successResult).when(depositRequester).requestDeposit(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class)
        );

        // when
        depositService.deposit(exchangeRequest);

        // then
        // 1. 람다식 캡처 확인
        verify(depositRequester).requestDeposit(
                eq(exchangeRequest),
                depositSucceededCaptor.capture(),
                depositFailedCaptor.capture(),
                depositResultUnknownCaptor.capture()
        );

        // 2. 각 람다식의 동작 검증
        // 2.1 성공 람다 검증
        depositSucceededCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).depositSucceeded();

        // 2.2 실패 람다 검증
        depositFailedCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).depositFailed();

        // 2.3 지연 체크 람다 검증
        depositResultUnknownCaptor.getValue().publish(exchangeRequest, Count.zero().increase());
        verify(exchangeRequest).depositResultUnknown(Count.zero().increase());
    }
}