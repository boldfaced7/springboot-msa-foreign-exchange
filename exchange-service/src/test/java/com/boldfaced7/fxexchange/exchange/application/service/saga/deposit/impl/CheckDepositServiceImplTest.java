package com.boldfaced7.fxexchange.exchange.application.service.saga.deposit.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.cache.LoadExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.deposit.LoadDepositPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.deposit.SaveDepositPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.domain.exception.ExchangeRequestNotFoundException;
import com.boldfaced7.fxexchange.exchange.domain.model.Deposit;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RetryPolicy;
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
class CheckDepositServiceImplTest {

    @InjectMocks CheckDepositServiceImpl checkDepositService;

    @Mock LoadExchangeRequestCachePort loadExchangeRequestCachePort;
    @Mock LoadDepositPort loadDepositPort;
    @Mock SaveDepositPort saveDepositPort;
    @Mock PublishEventPort publishEventPort;
    @Mock RetryPolicy retryPolicy;
    @Mock ExchangeRequest exchange;
    @Mock Deposit deposit;

    @Test
    @DisplayName("입금 확인이 성공하고 저장된다")
    void checkDeposit_success_and_saved() {
        // Given
        when(loadExchangeRequestCachePort.loadByRequestId(REQUEST_ID)).thenReturn(Optional.of(exchange));
        when(loadDepositPort.loadDeposit(exchange)).thenReturn(deposit);
        when(deposit.isSuccess()).thenReturn(true);

        // When
        checkDepositService.checkDeposit(REQUEST_ID, COUNT_ONE);

        // Then
        verify(loadExchangeRequestCachePort).loadByRequestId(REQUEST_ID);
        verify(loadDepositPort).loadDeposit(exchange);
        verify(deposit).processCheckResult();
        verify(publishEventPort).publish(deposit);
        verify(saveDepositPort).saveDeposit(deposit);
    }

    @Test
    @DisplayName("입금 확인이 성공하지만 실패한 경우 저장되지 않는다")
    void checkDeposit_failed_not_saved() {
        // Given
        when(loadExchangeRequestCachePort.loadByRequestId(REQUEST_ID)).thenReturn(Optional.of(exchange));
        when(loadDepositPort.loadDeposit(exchange)).thenReturn(deposit);
        when(deposit.isSuccess()).thenReturn(false);

        // When
        checkDepositService.checkDeposit(REQUEST_ID, COUNT_ONE);

        // Then
        verify(loadExchangeRequestCachePort).loadByRequestId(REQUEST_ID);
        verify(loadDepositPort).loadDeposit(exchange);
        verify(deposit).processCheckResult();
        verify(publishEventPort).publish(deposit);
        verify(saveDepositPort, never()).saveDeposit(any());
    }

    @Test
    @DisplayName("환전 요청이 없으면 예외가 발생한다")
    void checkDeposit_exchangeRequest_not_found() {
        // Given
        when(loadExchangeRequestCachePort.loadByRequestId(REQUEST_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ExchangeRequestNotFoundException.class, 
                () -> checkDepositService.checkDeposit(REQUEST_ID, COUNT_ONE));
        
        verify(loadExchangeRequestCachePort).loadByRequestId(REQUEST_ID);
        verify(loadDepositPort, never()).loadDeposit(any());
        verify(publishEventPort, never()).publish(any());
        verify(saveDepositPort, never()).saveDeposit(any());
    }

    @Test
    @DisplayName("입금 조회 실패시 예외 처리 및 이벤트 발행")
    void checkDeposit_loadDeposit_exception() {
        // Given
        when(loadExchangeRequestCachePort.loadByRequestId(REQUEST_ID)).thenReturn(Optional.of(exchange));
        when(loadDepositPort.loadDeposit(exchange)).thenThrow(new RuntimeException("Load deposit failed"));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> checkDepositService.checkDeposit(REQUEST_ID, COUNT_ONE));

        verify(loadExchangeRequestCachePort).loadByRequestId(REQUEST_ID);
        verify(loadDepositPort).loadDeposit(exchange);
        verify(exchange).handleDepositCheckUnknown(COUNT_ONE, retryPolicy);
        verify(publishEventPort).publish(exchange);
        verify(saveDepositPort, never()).saveDeposit(any());
    }
} 