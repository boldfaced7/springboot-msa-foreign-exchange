package com.boldfaced7.fxexchange.exchange.application.service.saga.withdrawal.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.cache.LoadExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.withdrawal.LoadWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.withdrawal.SaveWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.domain.exception.ExchangeRequestNotFoundException;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.model.Withdrawal;
import com.boldfaced7.fxexchange.exchange.domain.vo.RetryPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.boldfaced7.fxexchange.exchange.application.util.TestConstraints.COUNT_ONE;
import static com.boldfaced7.fxexchange.exchange.application.util.TestConstraints.REQUEST_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckWithdrawalServiceImplTest {

    @InjectMocks CheckWithdrawalServiceImpl checkWithdrawalService;

    @Mock LoadExchangeRequestCachePort loadExchangeRequestCachePort;
    @Mock LoadWithdrawalPort loadWithdrawalPort;
    @Mock SaveWithdrawalPort saveWithdrawalPort;
    @Mock PublishEventPort publishEventPort;
    @Mock RetryPolicy retryPolicy;
    @Mock ExchangeRequest exchange;
    @Mock Withdrawal withdrawal;

    @Test
    @DisplayName("출금 확인이 성공하고 저장된다")
    void checkWithdrawal_success_and_saved() {
        // Given
        when(loadExchangeRequestCachePort.loadByRequestId(REQUEST_ID))
                .thenReturn(Optional.of(exchange));
        when(loadWithdrawalPort.loadWithdrawal(exchange))
                .thenReturn(withdrawal);
        when(withdrawal.isSuccess())
                .thenReturn(true);

        // When
        checkWithdrawalService.checkWithdrawal(REQUEST_ID, COUNT_ONE);

        // Then
        verify(loadExchangeRequestCachePort).loadByRequestId(REQUEST_ID);
        verify(loadWithdrawalPort).loadWithdrawal(exchange);
        verify(withdrawal).processCheckResult();
        verify(publishEventPort).publish(withdrawal);
        verify(saveWithdrawalPort).saveWithdrawal(withdrawal);
    }

    @Test
    @DisplayName("출금 확인이 성공하지만 실패한 경우 저장되지 않는다")
    void checkWithdrawal_failed_not_saved() {
        // Given
        when(loadExchangeRequestCachePort.loadByRequestId(REQUEST_ID))
                .thenReturn(Optional.of(exchange));
        when(loadWithdrawalPort.loadWithdrawal(exchange))
                .thenReturn(withdrawal);
        when(withdrawal.isSuccess())
                .thenReturn(false);

        // When
        checkWithdrawalService.checkWithdrawal(REQUEST_ID, COUNT_ONE);

        // Then
        verify(loadExchangeRequestCachePort).loadByRequestId(REQUEST_ID);
        verify(loadWithdrawalPort).loadWithdrawal(exchange);
        verify(withdrawal).processCheckResult();
        verify(publishEventPort).publish(withdrawal);
        verify(saveWithdrawalPort, never()).saveWithdrawal(any());
    }

    @Test
    @DisplayName("환전 요청이 없으면 예외가 발생한다")
    void checkWithdrawal_exchangeRequest_not_found() {
        // Given
        when(loadExchangeRequestCachePort.loadByRequestId(REQUEST_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ExchangeRequestNotFoundException.class,
                () -> checkWithdrawalService.checkWithdrawal(REQUEST_ID, COUNT_ONE));

        verify(loadExchangeRequestCachePort).loadByRequestId(REQUEST_ID);
        verify(loadWithdrawalPort, never()).loadWithdrawal(any());
        verify(publishEventPort, never()).publish(any());
        verify(saveWithdrawalPort, never()).saveWithdrawal(any());
    }

    @Test
    @DisplayName("출금 조회 실패시 예외 처리 및 이벤트 발행")
    void checkWithdrawal_loadWithdrawal_exception() {
        // Given
        when(loadExchangeRequestCachePort.loadByRequestId(REQUEST_ID))
                .thenReturn(Optional.of(exchange));
        when(loadWithdrawalPort.loadWithdrawal(exchange))
                .thenThrow(new RuntimeException("Load withdrawal failed"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> checkWithdrawalService.checkWithdrawal(REQUEST_ID, COUNT_ONE));

        verify(loadExchangeRequestCachePort).loadByRequestId(REQUEST_ID);
        verify(loadWithdrawalPort).loadWithdrawal(exchange);
        verify(exchange).handleWithdrawalCheckUnknown(COUNT_ONE, retryPolicy);
        verify(publishEventPort).publish(exchange);
        verify(saveWithdrawalPort, never()).saveWithdrawal(any());
    }
} 