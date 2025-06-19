package com.boldfaced7.fxexchange.exchange.application.service.deposit;

import com.boldfaced7.fxexchange.exchange.application.service.deposit.impl.CheckDepositServiceImpl;
import com.boldfaced7.fxexchange.exchange.application.service.deposit.impl.DepositResultCheckerImpl;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.ParamEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.SimpleEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater.ParamRequestUpdater;
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
class CheckDepositServiceImplTest {

    @InjectMocks
    private CheckDepositServiceImpl checkDepositService;

    @Mock
    private ExchangeRequestLoader exchangeRequestLoader;

    @Mock
    private DepositResultCheckerImpl depositResultChecker;

    @Mock
    private ExchangeRequestUpdater exchangeRequestUpdater;

    @Mock
    private ExchangeRequest exchangeRequest;

    private RequestId requestId;
    private DepositResult successResult;
    private DepositResult failureResult;

    @Captor
    private ArgumentCaptor<SimpleEventPublisher> successCheckedCaptor;

    @Captor
    private ArgumentCaptor<SimpleEventPublisher> failureCheckedCaptor;

    @Captor
    private ArgumentCaptor<ParamEventPublisher<Count>> delayingCheckRequiredCaptor;

    @BeforeEach
    void setUp() {
        requestId = new RequestId(1L);

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
    @DisplayName("입금 성공 확인 시, 입금 ID가 추가되어야 한다")
    void checkDeposit_Success() {
        // given
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);

        doReturn(successResult).when(depositResultChecker).loadDepositResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(Count.zero().increase())
        );

        // when
        checkDepositService.checkDeposit(requestId, Count.zero(), Direction.BUY);

        // then
        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(depositResultChecker).loadDepositResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(Count.zero().increase())
        );
        verify(exchangeRequestUpdater).update(
                eq(exchangeRequest),
                any(ParamRequestUpdater.class),
                eq(successResult.depositId())
        );
    }

    @Test
    @DisplayName("입금 실패 확인 시, exchangeRequest가 업데이트 되지 않아야 한다")
    void checkDeposit_Failure() {
        // given
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        doReturn(failureResult).when(depositResultChecker).loadDepositResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(Count.zero().increase())
        );

        // when
        checkDepositService.checkDeposit(requestId, Count.zero(), Direction.BUY);

        // then
        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(depositResultChecker).loadDepositResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(Count.zero().increase())
        );
        verify(exchangeRequestUpdater, never()).update(
                eq(exchangeRequest),
                any(ParamRequestUpdater.class),
                eq(failureResult.depositId())
        );
    }

    @Test
    @DisplayName("입금 결과 확인 실패 시, exchangeRequest가 업데이트 되지 않아야 한다")
    void checkDeposit_Retry() {
        // given
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        doThrow(new RuntimeException("Network error")).when(depositResultChecker).loadDepositResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(Count.zero().increase())
        );

        // when & then
        assertThatThrownBy(() -> checkDepositService.checkDeposit(requestId, Count.zero(), Direction.BUY))
                .isInstanceOf(RuntimeException.class);

        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(depositResultChecker).loadDepositResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(Count.zero().increase())
        );
        verify(exchangeRequestUpdater, never()).update(
                eq(exchangeRequest),
                any(ParamRequestUpdater.class),
                eq(failureResult.depositId())
        );
    }

    @Test
    @DisplayName("입금 결과 조회 시 전달하는 람다를 검증한다")
    void checkDeposit_LambdaVerification_WhenCountIsZero() {
        // given
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        doReturn(successResult).when(depositResultChecker).loadDepositResult(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class),
                any(SimpleEventPublisher.class),
                any(ParamEventPublisher.class),
                eq(Count.zero().increase())
        );
        // when
        checkDepositService.checkDeposit(requestId, Count.zero(), Direction.BUY);

        // then
        // 1. 람다식 캡처 확인
        verify(depositResultChecker).loadDepositResult(
                eq(exchangeRequest),
                successCheckedCaptor.capture(),
                failureCheckedCaptor.capture(),
                delayingCheckRequiredCaptor.capture(),
                eq(Count.zero().increase())
        );
        // 2. 각 람다식의 동작 검증
        // 2.1 성공 람다 검증
        successCheckedCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).depositSuccessChecked();

        // 2.2 실패 람다 검증
        failureCheckedCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).depositFailureChecked();

        // 2.3 지연 체크 람다 검증
        delayingCheckRequiredCaptor.getValue().publish(exchangeRequest, Count.zero().increase());
        verify(exchangeRequest).depositCheckUnknown(Count.zero().increase());
    }
}