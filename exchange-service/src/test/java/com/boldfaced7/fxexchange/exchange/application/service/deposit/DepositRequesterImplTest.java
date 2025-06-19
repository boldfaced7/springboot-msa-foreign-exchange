package com.boldfaced7.fxexchange.exchange.application.service.deposit;

import com.boldfaced7.fxexchange.exchange.application.port.out.RequestDepositPort;
import com.boldfaced7.fxexchange.exchange.application.service.deposit.impl.DepositRequesterImpl;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.ParamEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.SimpleEventPublisher;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.AccountCommandStatus;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositId;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositResult;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositRequesterImplTest {

    @InjectMocks
    private DepositRequesterImpl depositRequester;

    @Mock
    private ExchangeEventPublisher exchangeEventPublisher;

    @Mock
    private RequestDepositPort requestDepositPort;

    @Mock
    private ExchangeRequest exchangeRequest;

    @Captor
    private ArgumentCaptor<SimpleEventPublisher> simpleEventPublisherCaptor;

    @Captor
    private ArgumentCaptor<ParamEventPublisher<Count>> paramEventPublisherCaptor;

    private DepositResult successResult;
    private DepositResult failureResult;

    @BeforeEach
    void setUp() {
        successResult = new DepositResult(true, new AccountCommandStatus("SUCCESS"), new DepositId("deposit-123"));
        failureResult = new DepositResult(false, new AccountCommandStatus("FAILED"), new DepositId("deposit-456"));
    }

    @Test
    @DisplayName("입금 요청 성공 시, 성공 이벤트 발행 람다를 전달한다")
    void requestDeposit_Success() {
        // given
        // 1. 입금 요청 성공 설정
        when(requestDepositPort.deposit(eq(exchangeRequest))).thenReturn(successResult);

        // 2. 이벤트 발행 설정
        doNothing().when(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class)
        );
        doNothing().when(exchangeRequest).depositSucceeded();

        // when
        DepositResult result = depositRequester.requestDeposit(
                exchangeRequest,
                ExchangeRequest::depositSucceeded,
                ExchangeRequest::depositFailed,
                ExchangeRequest::depositResultUnknown
        );

        // then
        // 1. 입금 요청 결과 확인
        assertThat(result).isEqualTo(successResult);

        // 2. 입금 요청 호출 확인
        verify(requestDepositPort).deposit(exchangeRequest);

        // 3. 성공 이벤트 발행 람다 전달 확인
        verify(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                simpleEventPublisherCaptor.capture()
        );
        simpleEventPublisherCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).depositSucceeded();
    }

    @Test
    @DisplayName("입금 요청 실패 시, 실패 이벤트 발행 람다를 전달한다")
    void requestDeposit_Failure() {
        // given
        // 1. 입금 요청 실패 설정
        when(requestDepositPort.deposit(eq(exchangeRequest))).thenReturn(failureResult);

        // 2. 이벤트 발행 설정
        doNothing().when(exchangeEventPublisher).publishEvents(eq(exchangeRequest), any(SimpleEventPublisher.class));
        doNothing().when(exchangeRequest).depositFailed();

        // when
        DepositResult result = depositRequester.requestDeposit(
                exchangeRequest,
                ExchangeRequest::depositSucceeded,
                ExchangeRequest::depositFailed,
                ExchangeRequest::depositResultUnknown
        );

        // then
        // 1. 입금 요청 결과 확인
        assertThat(result).isEqualTo(failureResult);

        // 2. 입금 요청 호출 확인
        verify(requestDepositPort).deposit(exchangeRequest);

        // 3. 실패 이벤트 발행 람다 전달 확인
        verify(exchangeEventPublisher).publishEvents(eq(exchangeRequest), simpleEventPublisherCaptor.capture());
        simpleEventPublisherCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).depositFailed();
    }

    @Test
    @DisplayName("입금 요청 중 예외 발생 시, 예외 이벤트 발행 람다를 전달한다")
    void requestDeposit_Exception() {
        // given
        // 1. 입금 요청 예외 발생 설정
        when(requestDepositPort.deposit(eq(exchangeRequest))).thenThrow(new RuntimeException("Network error"));

        // 2. 이벤트 발행 설정
        doNothing().when(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                any(ParamEventPublisher.class),
                eq(Count.zero())
        );
        doNothing().when(exchangeRequest).depositResultUnknown(eq(Count.zero()));

        // when & then
        // 1. 예외 발생 확인
        assertThatThrownBy(() -> depositRequester.requestDeposit(
                exchangeRequest,
                ExchangeRequest::depositSucceeded,
                ExchangeRequest::depositFailed,
                ExchangeRequest::depositResultUnknown
        )).isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Network error");

        // 2. 입금 요청 호출 확인
        verify(requestDepositPort).deposit(exchangeRequest);

        // 3. 예외 이벤트 발행 람다 전달 확인
        verify(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                paramEventPublisherCaptor.capture(),
                eq(Count.zero())
        );
        paramEventPublisherCaptor.getValue().publish(exchangeRequest, Count.zero());
        verify(exchangeRequest).depositResultUnknown(Count.zero());
    }
}