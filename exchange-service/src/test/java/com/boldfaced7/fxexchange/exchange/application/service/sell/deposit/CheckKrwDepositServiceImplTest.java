package com.boldfaced7.fxexchange.exchange.application.service.sell.deposit;

import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.sell.LoadKrwDepositResultPort;
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
class CheckKrwDepositServiceImplTest {

    @InjectMocks
    private CheckKrwDepositServiceImpl checkKrwDepositService;

    @Mock
    private LoadKrwDepositResultPort loadKrwDepositResultPort;

    @Mock
    private UpdateExchangeRequestPort updateExchangeRequestPort;

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
    private DepositResult successResult;
    private DepositResult failureResult;

    @BeforeEach
    void setUp() {
        requestId = new RequestId(1L);
        exchangeId = new ExchangeId("exchange-123");
        successResult = new DepositResult(true, new AccountCommandStatus("SUCCESS"), new DepositId("deposit-123"));
        failureResult = new DepositResult(false, new AccountCommandStatus("FAILED"), new DepositId("deposit-456"));
    }

    @Test
    @DisplayName("원화 입금 성공 시 입금 ID가 추가되고 환전(외화 판매) 완료 이벤트가 발행되어야 한다")
    void checkKrwDepositSuccess() {
        // given
        // 0. 도메인 객체 설정
        when(exchangeRequest.getExchangeId()).thenReturn(exchangeId);

        // 1. 원화 입금 결과 조회
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        when(loadKrwDepositResultPort.loadKrwDepositResult(exchangeId)).thenReturn(successResult);

        // 2. 환전(외화 판매) 완료 이벤트 발행
        doNothing().when(exchangeRequest).sellingCompleted();
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // 3. 원화 입금 ID 추가 및 업데이트
        doNothing().when(exchangeRequest).addDepositId(successResult.depositId());
        when(updateExchangeRequestPort.update(exchangeRequest)).thenReturn(exchangeRequest);

        // when
        checkKrwDepositService.checkKrwDeposit(requestId, Count.zero());

        // then
        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(loadKrwDepositResultPort).loadKrwDepositResult(exchangeId);

        verify(exchangeRequest).sellingCompleted();
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);

        verify(exchangeRequest).addDepositId(successResult.depositId());
        verify(updateExchangeRequestPort).update(exchangeRequest);
    }

    @Test
    @DisplayName("원화 입금 실패 시 원화 출금 취소 필요 이벤트가 발행되어야 한다")
    void checkKrwDepositFailure() {
        // given
        // 0. 도메인 객체 설정
        when(exchangeRequest.getExchangeId()).thenReturn(exchangeId);

        // 1. 원화 입금 결과 조회
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        when(loadKrwDepositResultPort.loadKrwDepositResult(exchangeId)).thenReturn(failureResult);

        // 2. 원화 출금 취소 필요 이벤트 발행
        doNothing().when(exchangeRequest).cancelingFxWithdrawalRequired();
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // when
        checkKrwDepositService.checkKrwDeposit(requestId, Count.zero());

        // then
        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(loadKrwDepositResultPort).loadKrwDepositResult(exchangeId);

        verify(exchangeRequest).cancelingFxWithdrawalRequired();
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);
    }

    @Test
    @DisplayName("원화 입금 결과 조회 실패 시 재시도 횟수가 최대값보다 작으면 지연 체크 이벤트가 발행되어야 한다")
    void checkKrwDepositRetry() {
        // given
        // 0. 도메인 객체 설정
        when(exchangeRequest.getExchangeId()).thenReturn(exchangeId);

        // 1. 원화 입금 결과 조회
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        when(loadKrwDepositResultPort.loadKrwDepositResult(exchangeId))
                .thenThrow(new RuntimeException("Network error"));

        // 2. 지연 체크 필요 이벤트 발행
        doNothing().when(exchangeRequest).delayingKrwDepositCheckRequired(new Count(1));
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // when & then
        assertThatThrownBy(() -> checkKrwDepositService.checkKrwDeposit(requestId, Count.zero()))
                .isInstanceOf(RuntimeException.class);

        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(loadKrwDepositResultPort).loadKrwDepositResult(exchangeId);

        verify(exchangeRequest).delayingKrwDepositCheckRequired(new Count(1));
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);
    }

    @Test
    @DisplayName("원화 입금 결과 조회 실패 시 재시도 횟수가 최대값이면 경고 메시지가 발행되어야 한다")
    void checkKrwDepositMaxRetry() {
        // given
        // 0. 도메인 객체 설정
        when(exchangeRequest.getRequestId()).thenReturn(requestId);
        when(exchangeRequest.getExchangeId()).thenReturn(exchangeId);

        // 1. 원화 입금 결과 조회
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        when(loadKrwDepositResultPort.loadKrwDepositResult(exchangeId))
                .thenThrow(new RuntimeException("Network error"));

        // 2. 경고 메시지 발송
        doNothing().when(warningMessageSender).sendWarningMessage(requestId, exchangeId);

        // when & then
        assertThatThrownBy(() -> checkKrwDepositService.checkKrwDeposit(requestId, new Count(3)))
                .isInstanceOf(RuntimeException.class);

        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(loadKrwDepositResultPort).loadKrwDepositResult(exchangeId);
        
        verify(warningMessageSender).sendWarningMessage(requestId, exchangeId);
    }
} 