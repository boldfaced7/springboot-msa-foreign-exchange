package com.boldfaced7.fxexchange.exchange.application.service.buy.withdrawal;

import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.buy.RequestKrwWithdrawalPort;
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
class WithdrawKrwServiceImplTest {

    @InjectMocks
    private WithdrawKrwServiceImpl withdrawKrwService;

    @Mock
    private RequestKrwWithdrawalPort requestKrwWithdrawalPort;

    @Mock
    private UpdateExchangeRequestPort updateExchangeRequestPort;

    @Mock
    private ExchangeEventPublisher exchangeEventPublisher;

    @Mock
    private ExchangeRequest exchangeRequest;

    private WithdrawalResult successResult;
    private WithdrawalResult failureResult;

    @BeforeEach
    void setUp() {
        successResult = new WithdrawalResult(true, new AccountCommandStatus("SUCCESS"),new WithdrawalId("withdrawal-123"));
        failureResult = new WithdrawalResult(false, new AccountCommandStatus("FAILED"),new WithdrawalId("withdrawal-456"));
    }

    @Test
    @DisplayName("원화 출금이 성공적으로 처리되어야 한다")
    void withdrawKrwSuccess() {
        // given
        // 1. 원화 출금 요청
        when(requestKrwWithdrawalPort.withdrawKrw(exchangeRequest)).thenReturn(successResult);

        // 2. 원화 출금 완료 이벤트 발행
        doNothing().when(exchangeRequest).krwWithdrawalCompleted();
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // 3. 원화 출금 결과 저장 및 업데이트
        doNothing().when(exchangeRequest).addWithdrawalId(successResult.withdrawalId());
        when(updateExchangeRequestPort.update(exchangeRequest)).thenReturn(exchangeRequest);

        // when
        WithdrawalDetail result = withdrawKrwService.withdrawKrw(exchangeRequest);

        // then
        // 1. 결과 확인
        assertThat(result).isNotNull();
        assertThat(result.exchangeRequest()).isEqualTo(exchangeRequest);
        assertThat(result.withdrawalResult()).isEqualTo(successResult);

        // 2. 요청 확인
        verify(requestKrwWithdrawalPort).withdrawKrw(exchangeRequest);

        verify(exchangeRequest).krwWithdrawalCompleted();
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);

        verify(exchangeRequest).addWithdrawalId(successResult.withdrawalId());
        verify(updateExchangeRequestPort).update(exchangeRequest);
    }

    @Test
    @DisplayName("원화 출금 실패 시 환전(외화 구매) 실패 이벤트가 발행되고, 예외가 발생해야 한다")
    void withdrawKrwFailure() {
        // given
        // 1. 원화 출금 요청
        when(requestKrwWithdrawalPort.withdrawKrw(exchangeRequest)).thenReturn(failureResult);

        // 2. 환전(외화 구매) 실패 이벤트 발행
        doNothing().when(exchangeRequest).buyingFailed();
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // when & then
        // 1. 예외 발생 확인
        assertThatThrownBy(() -> withdrawKrwService.withdrawKrw(exchangeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Withdraw failed");

        // 2. 요청 확인
        verify(requestKrwWithdrawalPort).withdrawKrw(exchangeRequest);

        verify(exchangeRequest).buyingFailed();
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);
    }

    @Test
    @DisplayName("원화 출금 요청 중 예외 발생 시 원화 출금 확인 필요 이벤트가 발행되어야 한다")
    void withdrawKrwException() {
        // given
        // 1. 원화 출금 요청
        when(requestKrwWithdrawalPort.withdrawKrw(exchangeRequest))
                .thenThrow(new RuntimeException("Network error"));

        // 2. 원화 출금 확인 요청 필요 이벤트 발행
        doNothing().when(exchangeRequest).checkingKrwWithdrawalRequired(Count.zero());
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // when & then
        // 1. 예외 발생 확인
        assertThatThrownBy(() -> withdrawKrwService.withdrawKrw(exchangeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Network error");

        // 2. 요청 확인
        verify(requestKrwWithdrawalPort).withdrawKrw(exchangeRequest);

        verify(exchangeRequest).checkingKrwWithdrawalRequired(Count.zero());
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);
    }
}