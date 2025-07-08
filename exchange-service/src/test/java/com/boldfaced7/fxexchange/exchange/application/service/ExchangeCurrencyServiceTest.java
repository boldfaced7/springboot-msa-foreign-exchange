package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.exchange.SaveExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.service.saga.ExchangeCurrencySagaOrchestrator;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.boldfaced7.fxexchange.exchange.application.util.TestConstraints.EXCHANGE_CURRENCY_COMMAND;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeCurrencyServiceTest {

    @InjectMocks ExchangeCurrencyService exchangeCurrencyService;

    @Mock SaveExchangeRequestPort saveExchangeRequestPort;
    @Mock PublishEventPort publishEventPort;
    @Mock ExchangeCurrencySagaOrchestrator exchangeCurrencySagaOrchestrator;
    @Mock ExchangeRequest savedExchange;

    ExchangeDetail expectedExchangeDetail = new ExchangeDetail(savedExchange, null, null);

    @Test
    @DisplayName("환전 요청이 성공적으로 처리된다")
    void exchangeCurrency_success() {
        // Given
        when(saveExchangeRequestPort.save(any(ExchangeRequest.class)))
                .thenReturn(savedExchange);
        when(exchangeCurrencySagaOrchestrator.startExchange(savedExchange))
                .thenReturn(expectedExchangeDetail);

        // When
        ExchangeDetail result = exchangeCurrencyService
                .exchangeCurrency(EXCHANGE_CURRENCY_COMMAND);

        // Then
        assertNotNull(result);

        verify(saveExchangeRequestPort).save(any(ExchangeRequest.class));
        verify(savedExchange).markExchangeCurrencyStarted();
        verify(publishEventPort).publish(savedExchange);
        verify(exchangeCurrencySagaOrchestrator).startExchange(savedExchange);

    }
} 