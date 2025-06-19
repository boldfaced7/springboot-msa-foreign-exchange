package com.boldfaced7.fxexchange.exchange.application.service.deposit;

import com.boldfaced7.fxexchange.exchange.application.port.out.LoadDepositResultPort;
import com.boldfaced7.fxexchange.exchange.application.service.deposit.impl.DepositResultCheckerImpl;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.ParamEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.SimpleEventPublisher;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositResultCheckerImplTest {

    @InjectMocks
    private DepositResultCheckerImpl depositResultChecker;

    @Mock
    private ExchangeEventPublisher exchangeEventPublisher;

    @Mock
    private LoadDepositResultPort loadDepositResultPort;

    @Mock
    private ExchangeRequest exchangeRequest;

    @Captor
    private ArgumentCaptor<SimpleEventPublisher> simpleEventPublisherCaptor;

    @Captor
    private ArgumentCaptor<ParamEventPublisher<Count>> paramEventPublisherCaptor;

    private DepositResult successResult;
    private DepositResult failureResult;
    private Count count;

    @BeforeEach
    void setUp() {
        when(exchangeRequest.getDirection()).thenReturn(Direction.BUY);
        when(exchangeRequest.getExchangeId()).thenReturn(new ExchangeId("exchangeId"));

        successResult = new DepositResult(true, new AccountCommandStatus("SUCCESS"), new DepositId("deposit-123"));
        failureResult = new DepositResult(false, new AccountCommandStatus("FAILED"), new DepositId("deposit-456"));
        count = Count.zero();
    }

    @Test
    @DisplayName("입금 결과 조회 성공 시, 성공 이벤트 발행 람다를 전달한다")
    void loadDepositResult_Success() {
        // given
        // 1. 입금 결과 조회 성공 설정
        when(loadDepositResultPort.loadDepositResult(
                exchangeRequest.getExchangeId(),
                Direction.BUY
        )).thenReturn(successResult);

        // 2. 이벤트 발행 설정
        doNothing().when(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class)
        );
        doNothing().when(exchangeRequest).depositSucceeded();

        // when
        DepositResult result = depositResultChecker.loadDepositResult(
                exchangeRequest,
                ExchangeRequest::depositSucceeded,
                ExchangeRequest::depositFailed,
                ExchangeRequest::depositCheckUnknown,
                count
        );

        // then
        // 1. 입금 결과 조회 결과 확인
        assertThat(result).isEqualTo(successResult);

        // 2. 입금 결과 조회 호출 확인
        verify(loadDepositResultPort).loadDepositResult(exchangeRequest.getExchangeId(), Direction.BUY);

        // 3. 성공 이벤트 발행 확인
        verify(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                simpleEventPublisherCaptor.capture()
        );
        simpleEventPublisherCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).depositSucceeded();
    }

    @Test
    @DisplayName("입금 결과 조회 실패 시, 실패 이벤트 발행 람다를 전달한다")
    void loadDepositResult_Failure() {
        // given
        // 1. 입금 결과 조회 실패 설정
        when(loadDepositResultPort.loadDepositResult(
                exchangeRequest.getExchangeId(),
                Direction.BUY
        )).thenReturn(failureResult);

        // 2. 이벤트 발행 설정
        doNothing().when(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class)
        );
        doNothing().when(exchangeRequest).depositFailed();

        // when
        DepositResult result = depositResultChecker.loadDepositResult(
                exchangeRequest,
                ExchangeRequest::depositSucceeded,
                ExchangeRequest::depositFailed,
                ExchangeRequest::depositCheckUnknown,
                count
        );

        // then
        // 1. 입금 결과 조회 결과 확인
        assertThat(result).isEqualTo(failureResult);

        // 2. 입금 결과 조회 호출 확인
        verify(loadDepositResultPort).loadDepositResult(exchangeRequest.getExchangeId(), Direction.BUY);

        // 3. 실패 이벤트 발행 확인
        verify(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                simpleEventPublisherCaptor.capture()
        );
        simpleEventPublisherCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).depositFailed();
    }

    @Test
    @DisplayName("입금 결과 조회 중 예외 발생 시, 예외 이벤트 발행 람다를 전달한다")
    @SuppressWarnings("unchecked")
    void loadDepositResult_Exception() {
        // given
        // 1. 입금 결과 조회 예외 발생 설정
        when(loadDepositResultPort.loadDepositResult(
                exchangeRequest.getExchangeId(),
                Direction.BUY
        )).thenThrow(new RuntimeException("Network error"));

        // 2. 이벤트 발행 설정
        doNothing().when(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                any(ParamEventPublisher.class),
                eq(count)
        );
        doNothing().when(exchangeRequest).depositCheckUnknown(any(Count.class));

        // when & then
        // 1. 예외 발생 확인
        assertThatThrownBy(() -> depositResultChecker.loadDepositResult(
                exchangeRequest,
                ExchangeRequest::depositSucceeded,
                ExchangeRequest::depositFailed,
                ExchangeRequest::depositCheckUnknown,
                count
        )).isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Network error");

        // 2. 입금 결과 조회 호출 확인
        verify(loadDepositResultPort).loadDepositResult(exchangeRequest.getExchangeId(), Direction.BUY);

        // 3. 예외 이벤트 발행 확인
        verify(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                paramEventPublisherCaptor.capture(),
                eq(count)
        );
        paramEventPublisherCaptor.getValue().publish(exchangeRequest, count);
        verify(exchangeRequest).depositCheckUnknown(count);
    }
} 