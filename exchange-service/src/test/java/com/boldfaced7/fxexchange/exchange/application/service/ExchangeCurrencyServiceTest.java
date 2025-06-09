package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.exchange.application.port.in.ExchangeCurrencyCommand;
import com.boldfaced7.fxexchange.exchange.application.port.out.SaveExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.saga.ExchangeCurrencySagaOrchestrator;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.SimpleEventPublisher;
import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeCurrencyServiceTest {

    @InjectMocks
    private ExchangeCurrencyService exchangeCurrencyService;

    @Mock
    private SaveExchangeRequestPort saveExchangeRequestPort;

    @Mock
    private ExchangeEventPublisher exchangeEventPublisher;

    @Mock
    private ExchangeCurrencySagaOrchestrator exchangeCurrencySagaOrchestrator;

    @Mock
    private ExchangeRequest exchangeRequest;

    @Mock
    private ExchangeDetail expectedDetail;

    private ExchangeCurrencyCommand command;

    @Captor
    private ArgumentCaptor<SimpleEventPublisher> exchangeCurrencyStartedCaptor;

    @BeforeEach
    void setUp() {
        command = new ExchangeCurrencyCommand(
                new UserId("userId"),
                new BaseCurrency(CurrencyCode.USD),
                new BaseAmount(100),
                new QuoteAmount(130000),
                Direction.BUY,
                new ExchangeRate(1300.0)
        );
    }

    @Test
    @DisplayName("환전 거래를 성공적으로 처리한다")
    void exchangeCurrency_Success() {
        // given
        doReturn(exchangeRequest).when(saveExchangeRequestPort).save(
                any(ExchangeRequest.class)
        );
        doNothing().when(exchangeEventPublisher).publishEvents(
                eq(command.exchangeId()),
                any(SimpleEventPublisher.class)
        );
        doReturn(expectedDetail).when(exchangeCurrencySagaOrchestrator).startExchange(
                any(ExchangeRequest.class)
        );

        // when
        ExchangeDetail result = exchangeCurrencyService.exchangeCurrency(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedDetail);

        verify(saveExchangeRequestPort).save(
                any(ExchangeRequest.class)
        );
        verify(exchangeEventPublisher).publishEvents(
                eq(command.exchangeId()),
                any(SimpleEventPublisher.class)
        );
        verify(exchangeCurrencySagaOrchestrator).startExchange(
                any(ExchangeRequest.class)
        );
    }

    @Test
    @DisplayName("이벤트 발행기에 전달하는 람다를 검증한다")
    void exchangeCurrency_LambdaVerification() {
        // given
        doReturn(exchangeRequest).when(saveExchangeRequestPort).save(
                any(ExchangeRequest.class)
        );
        doNothing().when(exchangeEventPublisher).publishEvents(
                eq(command.exchangeId()),
                any(SimpleEventPublisher.class)
        );
        doReturn(expectedDetail).when(exchangeCurrencySagaOrchestrator).startExchange(
                any(ExchangeRequest.class)
        );

        // when & then
        exchangeCurrencyService.exchangeCurrency(command);

        verify(saveExchangeRequestPort).save(
                any(ExchangeRequest.class)
        );
        verify(exchangeEventPublisher).publishEvents(
                eq(command.exchangeId()),
                exchangeCurrencyStartedCaptor.capture()
        );
        verify(exchangeCurrencySagaOrchestrator).startExchange(
                any(ExchangeRequest.class)
        );

        // 람다식의 동작 검증
        exchangeCurrencyStartedCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).exchangeCurrencyStarted();
    }

}