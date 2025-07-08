package com.boldfaced7.fxexchange.exchange.application.service.saga.exchange.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.exchange.LoadExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.exchange.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.domain.exception.ExchangeRequestNotFoundException;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.boldfaced7.fxexchange.exchange.application.util.TestConstraints.REQUEST_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompleteExchangeServiceImplTest {

    @InjectMocks CompleteExchangeServiceImpl completeExchangeService;

    @Mock LoadExchangeRequestPort loadExchangeRequestPort;
    @Mock UpdateExchangeRequestPort updateExchangeRequestPort;
    @Mock PublishEventPort publishEventPort;
    @Mock ExchangeRequest exchange;

    @Test
    @DisplayName("환전 성공 처리가 정상적으로 완료된다")
    void succeedExchange_success() {
        // Given
        when(loadExchangeRequestPort.loadByRequestIdForUpdate(REQUEST_ID))
                .thenReturn(Optional.of(exchange));
        when(updateExchangeRequestPort.update(exchange))
                .thenReturn(exchange);

        // When
        ExchangeRequest result = completeExchangeService.succeedExchange(REQUEST_ID);

        // Then
        assertEquals(exchange, result);
        verify(loadExchangeRequestPort).loadByRequestIdForUpdate(REQUEST_ID);
        verify(exchange).completeExchange(true);
        verify(publishEventPort).publish(exchange);
        verify(updateExchangeRequestPort).update(exchange);
    }

    @Test
    @DisplayName("환전 실패 처리가 정상적으로 완료된다")
    void failExchange_success() {
        // Given
        when(loadExchangeRequestPort.loadByRequestIdForUpdate(REQUEST_ID))
                .thenReturn(Optional.of(exchange));
        when(updateExchangeRequestPort.update(exchange))
                .thenReturn(exchange);

        // When
        ExchangeRequest result = completeExchangeService.failExchange(REQUEST_ID);

        // Then
        assertEquals(exchange, result);
        verify(loadExchangeRequestPort).loadByRequestIdForUpdate(REQUEST_ID);
        verify(exchange).completeExchange(false);
        verify(publishEventPort).publish(exchange);
        verify(updateExchangeRequestPort).update(exchange);
    }

    @Test
    @DisplayName("환전 요청이 없으면 예외가 발생한다 - 성공 케이스")
    void succeedExchange_exchangeRequest_not_found() {
        // Given
        when(loadExchangeRequestPort.loadByRequestIdForUpdate(REQUEST_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ExchangeRequestNotFoundException.class,
                () -> completeExchangeService.succeedExchange(REQUEST_ID));

        verify(loadExchangeRequestPort).loadByRequestIdForUpdate(REQUEST_ID);
    }

    @Test
    @DisplayName("환전 요청이 없으면 예외가 발생한다 - 실패 케이스")
    void failExchange_exchangeRequest_not_found() {
        // Given
        when(loadExchangeRequestPort.loadByRequestIdForUpdate(REQUEST_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ExchangeRequestNotFoundException.class,
                () -> completeExchangeService.failExchange(REQUEST_ID));

        verify(loadExchangeRequestPort).loadByRequestIdForUpdate(REQUEST_ID);
    }
} 