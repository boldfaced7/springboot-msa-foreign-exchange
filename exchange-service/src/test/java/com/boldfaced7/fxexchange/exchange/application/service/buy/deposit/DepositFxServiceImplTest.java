package com.boldfaced7.fxexchange.exchange.application.service.buy.deposit;

import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.buy.RequestFxDepositPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositFxServiceImplTest {

    @InjectMocks
    private DepositFxServiceImpl depositFxService;

    @Mock
    private RequestFxDepositPort requestFxDepositPort;

    @Mock
    private UpdateExchangeRequestPort updateExchangeRequestPort;

    @Mock
    private ExchangeEventPublisher exchangeEventPublisher;

    @Mock
    private ExchangeRequest exchangeRequest;

    private DepositResult successResult;
    private DepositResult failureResult;

    @BeforeEach
    void setUp() {
        successResult = new DepositResult(true, new AccountCommandStatus("SUCCESS"),new DepositId("deposit-123"));
        failureResult = new DepositResult(false, new AccountCommandStatus("FAILED"),new DepositId("deposit-456"));
    }

    @Test
    @DisplayName("외화 입금이 성공적으로 처리되어야 한다")
    void depositFxSuccess() {
        // given
        // 1. 외화 입금 요청
        when(requestFxDepositPort.depositFx(exchangeRequest)).thenReturn(successResult);

        // 2. 환전(외화 구매) 완료 이벤트 발행
        doNothing().when(exchangeRequest).buyingCompleted();
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // 3. 외화 입금 결과 저장 및 업데이트
        doNothing().when(exchangeRequest).addDepositId(successResult.depositId());
        when(updateExchangeRequestPort.update(exchangeRequest)).thenReturn(exchangeRequest);

        // when
        DepositDetail result = depositFxService.depositFx(exchangeRequest);

        // then
        // 1. 결과 확인
        assertThat(result).isNotNull();
        assertThat(result.exchangeRequest()).isEqualTo(exchangeRequest);
        assertThat(result.depositResult()).isEqualTo(successResult);

        // 2. 요청 확인
        verify(requestFxDepositPort).depositFx(exchangeRequest);

        verify(exchangeRequest).buyingCompleted();
        verify(updateExchangeRequestPort).update(exchangeRequest);

        verify(exchangeRequest).addDepositId(successResult.depositId());
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);
    }

    @Test
    @DisplayName("외화 입금 실패 시, 원화 출금 취소 필요 이벤트가 발행되고 예외가 발생해야 한다")
    void depositFxFailure() {
        // given
        // 1. 외화 입금 요청
        when(requestFxDepositPort.depositFx(exchangeRequest)).thenReturn(failureResult);

        // 2. 원화 출금 취소 필요 이벤트 발행
        doNothing().when(exchangeRequest).cancelingKrwWithdrawalRequired();
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // when & then
        // 1. 예외 발생 확인
        assertThatThrownBy(() -> depositFxService.depositFx(exchangeRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Deposit failed: ");

        // 2. 요청 확인
        verify(requestFxDepositPort).depositFx(exchangeRequest);

        verify(exchangeRequest).cancelingKrwWithdrawalRequired();
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);
    }

    @Test
    @DisplayName("외화 입금 요청 중 예외 발생 시, 외화 입금 확인 필요 이벤트가 발행되어야 한다")
    void withdrawKrwException() {
        // given
        // 1. 외화 입금 요청
        when(requestFxDepositPort.depositFx(exchangeRequest))
                .thenThrow(new RuntimeException("Network error"));

        // 2. 외화 입금 확인 필요 이벤트 발행
        doNothing().when(exchangeRequest).checkingFxDepositRequired(Count.zero());
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // when & then
        // 1. 예외 발생 확인
        assertThatThrownBy(() -> depositFxService.depositFx(exchangeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Network error");

        // 2. 요청 확인
        verify(requestFxDepositPort).depositFx(exchangeRequest);

        verify(exchangeRequest).checkingFxDepositRequired(Count.zero());
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);
    }

} 