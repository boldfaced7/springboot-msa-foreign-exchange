package com.boldfaced7.fxexchange.exchange.application.service.buy;

import com.boldfaced7.fxexchange.exchange.application.port.in.buy.BuyForeignCurrencyCommand;
import com.boldfaced7.fxexchange.exchange.application.port.out.SaveExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.saga.BuyForeignCurrencySagaOrchestrator;
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
class BuyForeignCurrencyServiceTest {

    @InjectMocks
    private BuyForeignCurrencyService buyForeignCurrencyService;

    @Mock
    private SaveExchangeRequestPort saveExchangeRequestPort;

    @Mock
    private ExchangeEventPublisher exchangeEventPublisher;

    @Mock
    private BuyForeignCurrencySagaOrchestrator buyForeignCurrencySagaOrchestrator;

    @Mock
    private ExchangeRequest exchangeRequest;

    @Mock
    private ExchangeDetail expectedDetail;

    private BuyForeignCurrencyCommand command;

    @BeforeEach
    void setUp() {
        command = new BuyForeignCurrencyCommand(
                new UserId("userId"),
                new QuoteCurrency(CurrencyCode.USD),
                new BaseAmount(100),
                new QuoteAmount(130000),
                new ExchangeRate(1300.0)
        );
    }

    @Test
    @DisplayName("외화 매수 거래를 성공적으로 처리한다")
    void buyForeignCurrency_Success() {
        // given
        // 1. 거래 요청 저장
        when(saveExchangeRequestPort.save(any(ExchangeRequest.class))).thenReturn(exchangeRequest);

        // 2. 환전(외화 구매) 시작 이벤트 발행
        doNothing().when(exchangeRequest).buyingStarted();
        doNothing().when(exchangeEventPublisher).publishEvents(exchangeRequest);

        // 3. 환전(외화 구매) 사가 오케스트레이터 호출
        when(buyForeignCurrencySagaOrchestrator.startExchange(exchangeRequest))
                .thenReturn(expectedDetail);

        // when
        ExchangeDetail result = buyForeignCurrencyService.buyForeignCurrency(command);

        // then
        assertThat(result).isNotNull();

        verify(saveExchangeRequestPort).save(any(ExchangeRequest.class));

        verify(exchangeRequest).buyingStarted();
        verify(exchangeEventPublisher).publishEvents(exchangeRequest);

        verify(buyForeignCurrencySagaOrchestrator).startExchange(exchangeRequest);
    }
}