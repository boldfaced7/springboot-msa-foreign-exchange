package com.boldfaced7.fxexchange.exchange.application.service.sell;

import com.boldfaced7.fxexchange.exchange.application.port.in.sell.CompleteFxWithdrawalCancelCommand;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompleteFxWithdrawalCancelServiceTest {

    @InjectMocks
    private CompleteFxWithdrawalCancelService completeFxWithdrawalCancelService;

    @Mock
    private ExchangeRequestLoader exchangeRequestLoader;

    @Mock
    private ExchangeEventPublisher exchangeEventPublisher;

    @Mock
    private ExchangeRequest exchangeRequest;

    @Test
    @DisplayName("외화 출금 취소 완료 요청 시, 환전(외화 판매) 실패 이벤트가 발행되어야 한다.")
    void completeFxWithdrawalCompensation() {
        // given
        ExchangeId exchangeId = new ExchangeId("exchange-id");
        CompleteFxWithdrawalCancelCommand command = new CompleteFxWithdrawalCancelCommand(exchangeId);

        // 1. 거래 요청 조회
        when(exchangeRequestLoader.loadExchangeRequest(exchangeId)).thenReturn(exchangeRequest);
        // 2. 환전(외화 판매) 실패 이벤트 발행
        doNothing().when(exchangeRequest).sellingFailed();
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // when
        completeFxWithdrawalCancelService.completeFxWithdrawalCompensation(command);

        // then
        verify(exchangeRequestLoader).loadExchangeRequest(exchangeId);

        verify(exchangeRequest).sellingFailed();
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);
    }
} 