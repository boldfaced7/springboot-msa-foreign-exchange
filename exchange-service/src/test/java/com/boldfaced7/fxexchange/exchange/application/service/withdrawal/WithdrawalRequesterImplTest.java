package com.boldfaced7.fxexchange.exchange.application.service.withdrawal;

import com.boldfaced7.fxexchange.exchange.application.port.out.RequestWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.ParamEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.SimpleEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.withdrawal.impl.WithdrawalRequesterImpl;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.AccountCommandStatus;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalResult;
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

@ExtendWith(MockitoExtension.class)
class WithdrawalRequesterImplTest {

    @InjectMocks
    private WithdrawalRequesterImpl withdrawalRequester;

    @Mock
    private ExchangeEventPublisher exchangeEventPublisher;

    @Mock
    private RequestWithdrawalPort requestWithdrawalPort;

    @Mock
    private ExchangeRequest exchangeRequest;

    @Captor
    private ArgumentCaptor<SimpleEventPublisher> simpleEventPublisherCaptor;

    @Captor
    private ArgumentCaptor<ParamEventPublisher<Count>> paramEventPublisherCaptor;

    private WithdrawalResult successResult;
    private WithdrawalResult failureResult;

    @BeforeEach
    void setUp() {
        successResult = new WithdrawalResult(true, new AccountCommandStatus("SUCCESS"), new WithdrawalId("withdrawal-123"));
        failureResult = new WithdrawalResult(false, new AccountCommandStatus("FAILED"), new WithdrawalId("withdrawal-456"));
    }

    @Test
    @DisplayName("출금 요청 성공 시, 성공 이벤트 발행 람다를 전달한다")
    void requestWithdrawal_Success() {
        // given
        // 1. 출금 요청 성공 설정
        when(requestWithdrawalPort.withdraw(exchangeRequest)).thenReturn(successResult);

        // 2. 이벤트 발행 설정
        doNothing().when(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class)
        );
        doNothing().when(exchangeRequest).withdrawalSucceeded();

        // when
        WithdrawalResult result = withdrawalRequester.requestWithdrawal(
                exchangeRequest,
                ExchangeRequest::withdrawalSucceeded,
                ExchangeRequest::withdrawalFailed,
                ExchangeRequest::withdrawalResultUnknown
        );

        // then
        // 1. 출금 요청 결과 확인
        assertThat(result).isEqualTo(successResult);

        // 2. 출금 요청 호출 확인
        verify(requestWithdrawalPort).withdraw(exchangeRequest);

        // 3. 성공 이벤트 발행 확인
        verify(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                simpleEventPublisherCaptor.capture()
        );
        simpleEventPublisherCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).withdrawalSucceeded();
    }

    @Test
    @DisplayName("출금 요청 실패 시, 실패 이벤트 발행 람다를 전달한다")
    void requestWithdrawal_Failure() {
        // given
        // 1. 출금 요청 실패 설정
        when(requestWithdrawalPort.withdraw(exchangeRequest)).thenReturn(failureResult);

        // 2. 이벤트 발행 설정
        doNothing().when(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class)
        );
        doNothing().when(exchangeRequest).withdrawalFailed();

        // when
        WithdrawalResult result = withdrawalRequester.requestWithdrawal(
                exchangeRequest,
                ExchangeRequest::withdrawalSucceeded,
                ExchangeRequest::withdrawalFailed,
                ExchangeRequest::withdrawalResultUnknown
        );

        // then
        // 1. 출금 요청 결과 확인
        assertThat(result).isEqualTo(failureResult);

        // 2. 출금 요청 호출 확인
        verify(requestWithdrawalPort).withdraw(exchangeRequest);

        // 3. 실패 이벤트 발행 확인
        verify(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                simpleEventPublisherCaptor.capture()
        );
        simpleEventPublisherCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).withdrawalFailed();
    }

    @Test
    @DisplayName("출금 요청 중 예외 발생 시, 예외 이벤트 발행 람다를 전달한다")
    void requestWithdrawal_Exception() {
        // given
        // 1. 출금 요청 예외 발생 설정
        when(requestWithdrawalPort.withdraw(exchangeRequest)).thenThrow(new RuntimeException("Network error"));

        // 2. 이벤트 발행 설정
        doNothing().when(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                any(ParamEventPublisher.class),
                eq(Count.zero())
        );
        doNothing().when(exchangeRequest).withdrawalResultUnknown(any(Count.class));

        // when & then
        // 1. 예외 발생 확인
        assertThatThrownBy(() -> withdrawalRequester.requestWithdrawal(
                exchangeRequest,
                ExchangeRequest::withdrawalSucceeded,
                ExchangeRequest::withdrawalFailed,
                ExchangeRequest::withdrawalResultUnknown
        )).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Network error");

        // 2. 출금 요청 호출 확인
        verify(requestWithdrawalPort).withdraw(exchangeRequest);

        // 3. 예외 이벤트 발행 확인
        verify(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                paramEventPublisherCaptor.capture(),
                eq(Count.zero())
        );
        paramEventPublisherCaptor.getValue().publish(exchangeRequest, Count.zero());
        verify(exchangeRequest).withdrawalResultUnknown(Count.zero());
    }
} 