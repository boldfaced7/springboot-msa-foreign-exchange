package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.exchange.application.port.in.CompleteWithdrawalCancelCommand;
import com.boldfaced7.fxexchange.exchange.application.port.out.cache.LoadExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.cancel.SaveWithdrawalCancelPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.domain.exception.ExchangeRequestNotFoundException;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.model.WithdrawalCancel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.boldfaced7.fxexchange.exchange.application.util.TestConstraints.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompleteWithdrawalCancelServiceTest {

    @InjectMocks CompleteWithdrawalCancelService completeWithdrawalCancelService;

    @Mock LoadExchangeRequestCachePort loadExchangeRequestCachePort;
    @Mock SaveWithdrawalCancelPort saveWithdrawalCancelPort;
    @Mock PublishEventPort publishEventPort;
    @Mock ExchangeRequest exchange;

    CompleteWithdrawalCancelCommand command
            = new CompleteWithdrawalCancelCommand(WITHDRAWAL_CANCEL_ID, EXCHANGE_ID, DIRECTION_BUY);

    @Test
    @DisplayName("출금 취소 완료가 정상적으로 처리된다")
    void completeWithdrawalCancel_success() {
        // Given
        when(loadExchangeRequestCachePort.loadByExchangeId(EXCHANGE_ID))
                .thenReturn(Optional.of(exchange));
        when(exchange.getRequestId()).thenReturn(REQUEST_ID);
        when(exchange.getUserId()).thenReturn(USER_ID);

        // When
        completeWithdrawalCancelService.completeWithdrawalCancel(command);

        // Then
        verify(loadExchangeRequestCachePort).loadByExchangeId(EXCHANGE_ID);
        verify(publishEventPort).publish(any(WithdrawalCancel.class));
        verify(saveWithdrawalCancelPort).saveWithdrawalCancel(any(WithdrawalCancel.class));
    }

    @Test
    @DisplayName("환전 요청이 없으면 예외가 발생한다")
    void completeWithdrawalCancel_exchangeRequest_not_found() {
        // Given
        when(loadExchangeRequestCachePort.loadByExchangeId(EXCHANGE_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ExchangeRequestNotFoundException.class,
                () -> completeWithdrawalCancelService.completeWithdrawalCancel(command));

        verify(loadExchangeRequestCachePort).loadByExchangeId(EXCHANGE_ID);
        verify(publishEventPort, never()).publish(any());
        verify(saveWithdrawalCancelPort, never()).saveWithdrawalCancel(any());
    }
} 