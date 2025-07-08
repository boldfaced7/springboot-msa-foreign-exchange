package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.exchange.application.port.in.CheckDepositWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.out.cache.LoadExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.domain.exception.ExchangeRequestNotFoundException;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.boldfaced7.fxexchange.exchange.application.util.TestConstraints.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckDepositWithDelayServiceTest {

    @InjectMocks CheckDepositWithDelayService checkDepositWithDelayService;

    @Mock LoadExchangeRequestCachePort loadExchangeRequestCachePort;
    @Mock PublishEventPort publishEventPort;
    @Mock ExchangeRequest exchange;

    CheckDepositWithDelayCommand command
            = new CheckDepositWithDelayCommand(EXCHANGE_ID, COUNT_ONE, DIRECTION_BUY);

    @Test
    @DisplayName("지연 후 입금 확인이 정상적으로 처리된다")
    void checkDepositWithDelay_success() {
        // Given
        when(loadExchangeRequestCachePort.loadByExchangeId(EXCHANGE_ID))
                .thenReturn(Optional.of(exchange));

        // When
        checkDepositWithDelayService.checkDepositWithDelay(command);

        // Then
        verify(loadExchangeRequestCachePort).loadByExchangeId(EXCHANGE_ID);
        verify(exchange).markDepositUnknown(COUNT_ONE);
        verify(publishEventPort).publish(exchange);
    }

    @Test
    @DisplayName("환전 요청이 없으면 예외가 발생한다")
    void checkDepositWithDelay_exchangeRequest_not_found() {
        // Given
        when(loadExchangeRequestCachePort.loadByExchangeId(EXCHANGE_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ExchangeRequestNotFoundException.class,
                () -> checkDepositWithDelayService.checkDepositWithDelay(command));

        verify(loadExchangeRequestCachePort).loadByExchangeId(EXCHANGE_ID);
    }
} 