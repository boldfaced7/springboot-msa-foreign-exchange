package com.boldfaced7.fxexchange.exchange.application.service.buy.withdrawal;

import com.boldfaced7.fxexchange.exchange.application.port.out.buy.LoadKrwWithdrawalResultPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import com.boldfaced7.fxexchange.exchange.application.service.util.WarningMessageSender;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckKrwWithdrawalServiceImplTest {

    @InjectMocks
    private CheckKrwWithdrawalServiceImpl checkKrwWithdrawalService;

    @Mock
    private LoadKrwWithdrawalResultPort loadKrwWithdrawalResultPort;

    @Mock
    private ExchangeRequestLoader exchangeRequestLoader;

    @Mock
    private ExchangeEventPublisher exchangeEventPublisher;

    @Mock
    private WarningMessageSender warningMessageSender;

    @Mock
    private ExchangeRequest exchangeRequest;

    private RequestId requestId;
    private ExchangeId exchangeId;
    private WithdrawalResult successResult;
    private WithdrawalResult failureResult;

    @BeforeEach
    void setUp() {
        requestId = new RequestId(1L);
        exchangeId = new ExchangeId("exchange-123");
        successResult = new WithdrawalResult(true, new AccountCommandStatus("SUCCESS"),new WithdrawalId("withdrawal-123"));
        failureResult = new WithdrawalResult(false, new AccountCommandStatus("FAILED"),new WithdrawalId("withdrawal-456"));
    }

    @Test
    @DisplayName("원화 출금 성공 확인 시 원화 출금 요청 취소 필요 이벤트가 발행되어야 한다")
    void checkKrwWithdrawalSuccess() {
        // given
        // 0. 도메인 객체 설정
        when(exchangeRequest.getExchangeId()).thenReturn(exchangeId);

        // 1. 원화 출금 결과 조회
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        when(loadKrwWithdrawalResultPort.loadKrwWithdrawalResult(exchangeId)).thenReturn(successResult);

        // 2. 원화 출금 요청 취소 필요 이벤트 발행
        doNothing().when(exchangeRequest).cancelingKrwWithdrawalRequired();
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // when
        checkKrwWithdrawalService.checkKrwWithdrawal(requestId, Count.zero());

        // then
        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(loadKrwWithdrawalResultPort).loadKrwWithdrawalResult(exchangeId);

        verify(exchangeRequest).cancelingKrwWithdrawalRequired();
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);
    }

    @Test
    @DisplayName("원화 출금 실패 확인 시 환전(외화 구매) 실패 이벤트가 발행되어야 한다")
    void checkKrwWithdrawalFailure() {
        // given
        // 0. 도메인 객체 설정
        when(exchangeRequest.getExchangeId()).thenReturn(exchangeId);

        // 1. 원화 출금 결과 조회
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        when(loadKrwWithdrawalResultPort.loadKrwWithdrawalResult(exchangeId)).thenReturn(failureResult);

        // 2. 환전(외화 구매) 실패 이벤트 발행
        doNothing().when(exchangeRequest).buyingFailed();
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // when
        checkKrwWithdrawalService.checkKrwWithdrawal(requestId, Count.zero());

        // then
        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(loadKrwWithdrawalResultPort).loadKrwWithdrawalResult(exchangeId);

        verify(exchangeRequest).buyingFailed();
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);
    }

    @Test
    @DisplayName("원화 출금 결과 조회 실패 시 재시도 횟수가 최대값보다 작으면 지연 체크 필요 이벤트가 발행되어야 한다")
    void checkKrwWithdrawalRetry() {
        // given
        // 0. 도메인 객체 설정
        when(exchangeRequest.getExchangeId()).thenReturn(exchangeId);

        // 1. 원화 출금 결과 조회
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        when(loadKrwWithdrawalResultPort.loadKrwWithdrawalResult(exchangeId))
                .thenThrow(new RuntimeException("Network error"));

        // 2. 지연 체크 필요 이벤트 발행
        doNothing().when(exchangeRequest).delayingKrwWithdrawalCheckRequired(new Count(1));
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // when & then
        assertThatThrownBy(() -> checkKrwWithdrawalService.checkKrwWithdrawal(requestId, Count.zero()))
                .isInstanceOf(RuntimeException.class);

        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(loadKrwWithdrawalResultPort).loadKrwWithdrawalResult(exchangeId);

        verify(exchangeRequest).delayingKrwWithdrawalCheckRequired(new Count(1));
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);
    }

    @Test
    @DisplayName("원화 출금 결과 조회 실패 시 재시도 횟수가 최대값이면 경고 메시지가 발송되어야 한다")
    void checkKrwWithdrawalMaxRetry() {
        // given
        // 0. 도메인 객체 설정
        when(exchangeRequest.getRequestId()).thenReturn(requestId);
        when(exchangeRequest.getExchangeId()).thenReturn(exchangeId);

        // 1. 원화 출금 결과 조회
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        when(loadKrwWithdrawalResultPort.loadKrwWithdrawalResult(exchangeId))
                .thenThrow(new RuntimeException("Network error"));

        // 2. 경고 메시지 발송
        doNothing().when(warningMessageSender).sendWarningMessage(requestId, exchangeId);

        // when & then
        assertThatThrownBy(() -> checkKrwWithdrawalService.checkKrwWithdrawal(requestId, new Count(3)))
                .isInstanceOf(RuntimeException.class);

        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(loadKrwWithdrawalResultPort).loadKrwWithdrawalResult(exchangeId);
        verify(warningMessageSender).sendWarningMessage(requestId, exchangeId);
    }
} 