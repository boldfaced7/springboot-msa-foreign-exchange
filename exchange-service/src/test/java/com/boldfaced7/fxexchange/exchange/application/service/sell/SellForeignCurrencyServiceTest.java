package com.boldfaced7.fxexchange.exchange.application.service.sell;

import com.boldfaced7.fxexchange.exchange.application.port.in.sell.SellForeignCurrencyCommand;
import com.boldfaced7.fxexchange.exchange.application.port.out.SaveExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.saga.SellForeignCurrencySagaOrchestrator;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellForeignCurrencyServiceTest {

    @InjectMocks
    private SellForeignCurrencyService sellForeignCurrencyService;

    @Mock
    private SaveExchangeRequestPort saveExchangeRequestPort;

    @Mock
    private ExchangeEventPublisher exchangeEventPublisher;

    @Mock
    private SellForeignCurrencySagaOrchestrator sellForeignCurrencySagaOrchestrator;

    @Mock
    private ExchangeRequest exchangeRequest;

    @Mock
    private ExchangeDetail expectedDetail;

    private SellForeignCurrencyCommand command;

    @BeforeEach
    void setUp() {
        command = new SellForeignCurrencyCommand(
                new UserId("userId"),
                new BaseCurrency(CurrencyCode.USD),
                new BaseAmount(100),
                new QuoteAmount(130000),
                new ExchangeRate(1300.0)
        );
    }

    @Test
    @DisplayName("외화 매도 거래를 성공적으로 처리한다")
    void sellForeignCurrency_Success() {
        // given
        // 1. 거래 요청 저장
        when(saveExchangeRequestPort.save(any(ExchangeRequest.class))).thenReturn(exchangeRequest);

        // 2. 환전(외화 판매) 시작 이벤트 발행
        doNothing().when(exchangeRequest).sellingStarted();
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // 3. 환전(외화 판매) 사가 오케스트레이터 호출
        when(sellForeignCurrencySagaOrchestrator.startExchange(exchangeRequest))
                .thenReturn(expectedDetail);

        // when
        ExchangeDetail result = sellForeignCurrencyService.sellForeignCurrency(command);

        // then
        assertThat(result).isNotNull();

        verify(saveExchangeRequestPort).save(any(ExchangeRequest.class));

        verify(exchangeRequest).sellingStarted();
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);

        verify(sellForeignCurrencySagaOrchestrator).startExchange(exchangeRequest);
    }
} 