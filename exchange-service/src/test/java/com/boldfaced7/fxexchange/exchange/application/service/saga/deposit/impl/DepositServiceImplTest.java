package com.boldfaced7.fxexchange.exchange.application.service.saga.deposit.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.cache.SaveExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.deposit.RequestDepositPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.deposit.SaveDepositPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.domain.model.Deposit;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.deposit.DepositDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositServiceImplTest {

    @InjectMocks DepositServiceImpl depositService;

    @Mock RequestDepositPort requestDepositPort;
    @Mock SaveDepositPort saveDepositPort;
    @Mock SaveExchangeRequestCachePort saveExchangeRequestCachePort;
    @Mock PublishEventPort publishEventPort;
    @Mock ExchangeRequest exchange;
    @Mock Deposit deposit;

    @Test
    @DisplayName("입금이 성공하고 저장된다")
    void deposit_success_and_saved() {
        // Given
        when(requestDepositPort.deposit(exchange)).thenReturn(deposit);
        when(deposit.isSuccess()).thenReturn(true);

        // When
        DepositDetail result = depositService.deposit(exchange);

        // Then
        assertNotNull(result);
        assertEquals(exchange, result.exchangeRequest());
        assertEquals(deposit, result.deposit());

        verify(requestDepositPort).deposit(exchange);
        verify(deposit).processTransactionResult();
        verify(publishEventPort).publish(deposit);
        verify(saveDepositPort).saveDeposit(deposit);
        verify(saveExchangeRequestCachePort, never()).save(any());
    }

    @Test
    @DisplayName("입금이 실패하면 캐시에 저장하고 예외를 발생시킨다")
    void deposit_failed_saves_cache_and_throws_exception() {
        // Given
        when(requestDepositPort.deposit(exchange)).thenReturn(deposit);
        when(deposit.isSuccess()).thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> depositService.deposit(exchange));

        assertEquals("입금 실패", exception.getMessage());

        verify(requestDepositPort).deposit(exchange);
        verify(deposit).processTransactionResult();
        verify(publishEventPort).publish(deposit);
        verify(saveExchangeRequestCachePort).save(exchange);
        verify(saveDepositPort, never()).saveDeposit(any());
    }

    @Test
    @DisplayName("입금 요청 중 예외 발생시 캐시에 저장하고 이벤트를 발행한다")
    void deposit_request_exception_saves_cache_and_publishes_event() {
        // Given
        RuntimeException exception = new RuntimeException("Deposit request failed");
        when(requestDepositPort.deposit(exchange)).thenThrow(exception);

        // When & Then
        RuntimeException thrownException = assertThrows(RuntimeException.class,
                () -> depositService.deposit(exchange));

        assertEquals("Deposit request failed", thrownException.getMessage());

        verify(requestDepositPort).deposit(exchange);
        verify(saveExchangeRequestCachePort).save(exchange);
        verify(exchange).markDepositUnknown(Count.zero());
        verify(publishEventPort).publish(exchange);
        verify(deposit, never()).processTransactionResult();
        verify(saveDepositPort, never()).saveDeposit(any());
    }
} 