package com.boldfaced7.fxexchange.exchange.application.service.sell.deposit;

import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.sell.RequestKrwDepositPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositKrwServiceImplTest {

    @InjectMocks
    private DepositKrwServiceImpl depositKrwService;

    @Mock
    private RequestKrwDepositPort requestKrwDepositPort;

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
    @DisplayName("원화 입금이 성공적으로 처리되어야 한다")
    void depositKrwSuccess() {
        // given
        // 1. 원화 입금 요청
        when(requestKrwDepositPort.depositKrw(any())).thenReturn(successResult);
        
        // 2. 환전(외화 판매) 완료 이벤트 발행
        doNothing().when(exchangeRequest).sellingCompleted();
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // 3. 원화 입금 결과 저장 및 업데이트
        doNothing().when(exchangeRequest).addDepositId(successResult.depositId());
        when(updateExchangeRequestPort.update(exchangeRequest)).thenReturn(exchangeRequest);

        // when
        DepositDetail result = depositKrwService.depositKrw(exchangeRequest);

        // then
        // 1. 결과 확인
        assertThat(result).isNotNull();
        assertThat(result.exchangeRequest()).isEqualTo(exchangeRequest);
        assertThat(result.depositResult()).isEqualTo(successResult);

        // 2. 요청 확인
        verify(requestKrwDepositPort).depositKrw(exchangeRequest);

        verify(exchangeRequest).sellingCompleted();
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);

        verify(exchangeRequest).addDepositId(successResult.depositId());
        verify(updateExchangeRequestPort).update(exchangeRequest);
    }

    @Test
    @DisplayName("원화 입금 실패 시, 원화 출금 취소 필요 이벤트가 발행되고 예외가 발생해야 한다")
    void depositKrwFailure() {
        // given
        // 1. 원화 입금 요청
        when(requestKrwDepositPort.depositKrw(exchangeRequest)).thenReturn(failureResult);

        // 2. 원화 출금 취소 필요 이벤트 발행
        doNothing().when(exchangeRequest).cancelingFxWithdrawalRequired();
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // when & then
        // 1. 예외 발생 확인
        assertThatThrownBy(() -> depositKrwService.depositKrw(exchangeRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Deposit failed: ");

        // 2. 요청 확인
        verify(requestKrwDepositPort).depositKrw(exchangeRequest);

        verify(exchangeRequest).cancelingFxWithdrawalRequired();
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);
    }

    @Test
    @DisplayName("원화 입금 요청 중 예외 발생 시, 입금 확인 필요 이벤트가 발행되어야 한다")
    void depositKrwException() {
        // given
        // 1. 원화 입금 요청
        when(requestKrwDepositPort.depositKrw(exchangeRequest))
                .thenThrow(new RuntimeException("Network error"));

        // 2. 원화 입금 확인 필요 이벤트 발행
        doNothing().when(exchangeRequest).checkingKrwDepositRequired(Count.zero());
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // when & then
        // 1. 예외 발생 확인
        assertThatThrownBy(() -> depositKrwService.depositKrw(exchangeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Network error");

        // 2. 요청 확인
        verify(requestKrwDepositPort).depositKrw(exchangeRequest);

        verify(exchangeRequest).checkingKrwDepositRequired(Count.zero());
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);
    }

} 