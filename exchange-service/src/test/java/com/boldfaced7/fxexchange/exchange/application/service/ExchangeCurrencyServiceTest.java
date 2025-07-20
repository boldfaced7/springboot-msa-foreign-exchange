package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.exchange.application.service.saga.ExchangeCurrencySagaOrchestrator;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeDetail;
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

    @Mock ExchangeCurrencySagaOrchestrator exchangeCurrencySagaOrchestrator;

    @Test
    @DisplayName("환전 요청이 성공적으로 처리된다")
    void exchangeCurrency_success() {
        // Given
        when(exchangeCurrencySagaOrchestrator.startExchange(any(ExchangeRequest.class)))
                .thenReturn(new ExchangeDetail((ExchangeRequest) null, null, null));

        // When
        ExchangeDetail result = exchangeCurrencyService
                .exchangeCurrency(EXCHANGE_CURRENCY_COMMAND);

        // Then
        assertNotNull(result);

        verify(exchangeCurrencySagaOrchestrator)
                .startExchange(any(ExchangeRequest.class));

    }
} 