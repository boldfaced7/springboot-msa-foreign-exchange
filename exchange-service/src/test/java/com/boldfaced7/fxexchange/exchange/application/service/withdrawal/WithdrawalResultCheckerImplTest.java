package com.boldfaced7.fxexchange.exchange.application.service.withdrawal;

import com.boldfaced7.fxexchange.exchange.application.port.out.LoadWithdrawalResultPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.ParamEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.SimpleEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.withdrawal.impl.WithdrawalResultCheckerImpl;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawalResultCheckerImplTest {

    @InjectMocks
    private WithdrawalResultCheckerImpl withdrawalResultChecker;

    @Mock
    private Map<Direction, LoadWithdrawalResultPort> loadWithdrawalResultPorts;

    @Mock
    private ExchangeEventPublisher exchangeEventPublisher;

    @Mock
    private LoadWithdrawalResultPort loadWithdrawalResultPort;

    @Mock
    private ExchangeRequest exchangeRequest;

    @Captor
    private ArgumentCaptor<SimpleEventPublisher> simpleEventPublisherCaptor;

    @Captor
    private ArgumentCaptor<ParamEventPublisher<Count>> paramEventPublisherCaptor;

    private WithdrawalResult successResult;
    private WithdrawalResult failureResult;
    private Count count;

    @BeforeEach
    void setUp() {
        when(exchangeRequest.getDirection()).thenReturn(Direction.BUY);
        when(loadWithdrawalResultPorts.get(Direction.BUY)).thenReturn(loadWithdrawalResultPort);

        successResult = new WithdrawalResult(true, new AccountCommandStatus("SUCCESS"), new WithdrawalId("withdrawal-123"));
        failureResult = new WithdrawalResult(false, new AccountCommandStatus("FAILED"), new WithdrawalId("withdrawal-456"));
        count = Count.zero();
    }

    @Test
    @DisplayName("출금 결과 조회 성공 시, 성공 이벤트 발행 람다를 전달한다")
    void loadWithdrawalResult_Success() {
        // given
        // 1. 출금 결과 조회 성공 설정
        when(loadWithdrawalResultPort.loadWithdrawalResult(exchangeRequest.getExchangeId())).thenReturn(successResult);

        // 2. 이벤트 발행 설정
        doNothing().when(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class)
        );
        doNothing().when(exchangeRequest).withdrawalSucceeded();

        // when
        WithdrawalResult result = withdrawalResultChecker.loadWithdrawalResult(
                exchangeRequest,
                ExchangeRequest::withdrawalSucceeded,
                ExchangeRequest::withdrawalFailed,
                ExchangeRequest::delayingWithdrawalCheckRequired,
                count
        );

        // then
        // 1. 출금 결과 조회 결과 확인
        assertThat(result).isEqualTo(successResult);

        // 2. 출금 결과 조회 호출 확인
        verify(loadWithdrawalResultPort).loadWithdrawalResult(exchangeRequest.getExchangeId());

        // 3. 성공 이벤트 발행 확인
        verify(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                simpleEventPublisherCaptor.capture()
        );
        simpleEventPublisherCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).withdrawalSucceeded();
    }

    @Test
    @DisplayName("출금 결과 조회 실패 시, 실패 이벤트 발행 람다를 전달한다")
    void loadWithdrawalResult_Failure() {
        // given
        // 1. 출금 결과 조회 실패 설정
        when(loadWithdrawalResultPort.loadWithdrawalResult(exchangeRequest.getExchangeId())).thenReturn(failureResult);

        // 2. 이벤트 발행 설정
        doNothing().when(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                any(SimpleEventPublisher.class)
        );
        doNothing().when(exchangeRequest).withdrawalFailed();

        // when
        WithdrawalResult result = withdrawalResultChecker.loadWithdrawalResult(
                exchangeRequest,
                ExchangeRequest::withdrawalSucceeded,
                ExchangeRequest::withdrawalFailed,
                ExchangeRequest::delayingWithdrawalCheckRequired,
                count
        );

        // then
        // 1. 출금 결과 조회 결과 확인
        assertThat(result).isEqualTo(failureResult);

        // 2. 출금 결과 조회 호출 확인
        verify(loadWithdrawalResultPort).loadWithdrawalResult(exchangeRequest.getExchangeId());

        // 3. 실패 이벤트 발행 확인
        verify(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                simpleEventPublisherCaptor.capture()
        );
        simpleEventPublisherCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).withdrawalFailed();
    }

    @Test
    @DisplayName("출금 결과 조회 중 예외 발생 시, 예외 이벤트 발행 람다를 전달한다")
    @SuppressWarnings("unchecked")
    void loadWithdrawalResult_Exception() {
        // given
        // 1. 출금 결과 조회 예외 발생 설정
        when(loadWithdrawalResultPort.loadWithdrawalResult(exchangeRequest.getExchangeId()))
                .thenThrow(new RuntimeException("Network error"));

        // 2. 이벤트 발행 설정
        doNothing().when(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                any(ParamEventPublisher.class),
                eq(count)
        );
        doNothing().when(exchangeRequest).delayingWithdrawalCheckRequired(any(Count.class));

        // when & then
        // 1. 예외 발생 확인
        assertThatThrownBy(() -> withdrawalResultChecker.loadWithdrawalResult(
                exchangeRequest,
                ExchangeRequest::withdrawalSucceeded,
                ExchangeRequest::withdrawalFailed,
                ExchangeRequest::delayingWithdrawalCheckRequired,
                count
        )).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Network error");

        // 2. 출금 결과 조회 호출 확인
        verify(loadWithdrawalResultPort).loadWithdrawalResult(exchangeRequest.getExchangeId());

        // 3. 예외 이벤트 발행 확인
        verify(exchangeEventPublisher).publishEvents(
                eq(exchangeRequest),
                paramEventPublisherCaptor.capture(),
                eq(count)
        );
        paramEventPublisherCaptor.getValue().publish(exchangeRequest, count);
        verify(exchangeRequest).delayingWithdrawalCheckRequired(count);
    }
} 