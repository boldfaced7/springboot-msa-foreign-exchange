package com.boldfaced7.fxexchange.exchange.application.service.sell;

import com.boldfaced7.fxexchange.exchange.application.port.in.sell.CheckFxWithdrawalWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckFxWithdrawalWithDelayServiceTest {

    @InjectMocks
    private CheckFxWithdrawalWithDelayService checkFxWithdrawalWithDelayService;

    @Mock
    private ExchangeRequestLoader exchangeRequestLoader;

    @Mock
    private ExchangeEventPublisher exchangeEventPublisher;

    @Mock
    private ExchangeRequest exchangeRequest;

    @Test
    @DisplayName("외화 출금 지연 확인 요청 시, 외화 출금 확인 필요 이벤트가 발행되어야 한다.")
    void checkFxWithdrawalWithDelay() {
        // given
        RequestId requestId = new RequestId(1L);
        Count count = new Count(1);
        CheckFxWithdrawalWithDelayCommand command = new CheckFxWithdrawalWithDelayCommand(requestId, count);

        // 1. 거래 요청 조회
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        // 2. 외화 출금 확인 필요 이벤트 발행
        doNothing().when(exchangeRequest).checkingFxWithdrawalRequired(count);
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // when
        checkFxWithdrawalWithDelayService.checkFxWithdrawalWithDelay(command);

        // then
        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        
        verify(exchangeRequest).checkingFxWithdrawalRequired(count);
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);
    }
} 