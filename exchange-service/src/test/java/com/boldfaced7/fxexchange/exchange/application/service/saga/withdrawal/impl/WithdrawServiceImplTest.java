package com.boldfaced7.fxexchange.exchange.application.service.saga.withdrawal.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.cache.SaveExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.withdrawal.RequestWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.withdrawal.SaveWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.model.Withdrawal;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.withdrawal.WithdrawalDetail;
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
class WithdrawServiceImplTest {

    @InjectMocks WithdrawServiceImpl withdrawService;

    @Mock RequestWithdrawalPort requestWithdrawalPort;
    @Mock SaveWithdrawalPort saveWithdrawalPort;
    @Mock SaveExchangeRequestCachePort saveExchangeRequestCachePort;
    @Mock PublishEventPort publishEventPort;
    @Mock ExchangeRequest exchange;
    @Mock Withdrawal withdrawal;

    @Test
    @DisplayName("출금이 성공하고 저장된다")
    void withdraw_success_and_saved() {
        // Given
        when(requestWithdrawalPort.withdraw(exchange))
                .thenReturn(withdrawal);
        when(withdrawal.isSuccess())
                .thenReturn(true);

        // When
        WithdrawalDetail result = withdrawService.withdraw(exchange);

        // Then
        assertNotNull(result);
        assertEquals(exchange, result.exchangeRequest());
        assertEquals(withdrawal, result.withdrawal());

        verify(requestWithdrawalPort).withdraw(exchange);
        verify(withdrawal).processTransactionResult();
        verify(publishEventPort).publish(withdrawal);
        verify(saveWithdrawalPort).saveWithdrawal(withdrawal);
        verify(saveExchangeRequestCachePort, never()).save(any());
    }

    @Test
    @DisplayName("출금이 실패하면 예외를 발생시킨다")
    void withdraw_failed_throws_exception() {
        // Given
        when(requestWithdrawalPort.withdraw(exchange))
                .thenReturn(withdrawal);
        when(withdrawal.isSuccess())
                .thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> withdrawService.withdraw(exchange));

        assertEquals("출금 실패", exception.getMessage());

        verify(requestWithdrawalPort).withdraw(exchange);
        verify(withdrawal).processTransactionResult();
        verify(publishEventPort).publish(withdrawal);
        verify(saveWithdrawalPort, never()).saveWithdrawal(any());
        verify(saveExchangeRequestCachePort, never()).save(any());
    }

    @Test
    @DisplayName("출금 요청 중 예외 발생시 캐시에 저장하고 이벤트를 발행한다")
    void withdraw_request_exception_saves_cache_and_publishes_event() {
        // Given
        when(requestWithdrawalPort.withdraw(exchange))
                .thenThrow(new RuntimeException("Withdrawal request failed"));

        // When & Then
        RuntimeException thrownException = assertThrows(RuntimeException.class,
                () -> withdrawService.withdraw(exchange));

        assertEquals("Withdrawal request failed", thrownException.getMessage());

        verify(requestWithdrawalPort).withdraw(exchange);
        verify(saveExchangeRequestCachePort).save(exchange);
        verify(exchange).markWithdrawalUnknown(Count.zero());
        verify(publishEventPort).publish(exchange);
        verify(withdrawal, never()).processTransactionResult();
        verify(saveWithdrawalPort, never()).saveWithdrawal(any());
    }
} 