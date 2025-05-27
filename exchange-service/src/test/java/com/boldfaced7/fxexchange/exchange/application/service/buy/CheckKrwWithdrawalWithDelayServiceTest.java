package com.boldfaced7.fxexchange.exchange.application.service.buy;

import com.boldfaced7.fxexchange.exchange.application.port.in.buy.CheckKrwWithdrawalWithDelayCommand;
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
class CheckKrwWithdrawalWithDelayServiceTest {

    @InjectMocks
    private CheckKrwWithdrawalWithDelayService checkKrwWithdrawalWithDelayService;

    @Mock
    private ExchangeRequestLoader exchangeRequestLoader;

    @Mock
    private ExchangeEventPublisher exchangeEventPublisher;

    @Mock
    private ExchangeRequest exchangeRequest;

    @Test
    @DisplayName("원화 출금 지연 확인 요청 시, 원화 출금 확인 필요 이벤트가 발행되어야 한다.")
    void checkKrwWithdrawalWithDelay() {
        // given
        RequestId requestId = new RequestId(1L);
        Count count = new Count(1);
        CheckKrwWithdrawalWithDelayCommand command = new CheckKrwWithdrawalWithDelayCommand(requestId, count);

        // 1. 거래 요청 조회
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        // 2. 원화 출금 확인 필요 이벤트 발행
        doNothing().when(exchangeRequest).checkingKrwWithdrawalRequired(count);
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // when
        checkKrwWithdrawalWithDelayService.checkKrwWithdrawalWithDelay(command);

        // then
        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        
        verify(exchangeRequest).checkingKrwWithdrawalRequired(count);
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);
    }
} 