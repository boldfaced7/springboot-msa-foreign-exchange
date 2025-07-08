package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.exchange.application.port.in.CheckWithdrawalWithDelayCommand;
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
class CheckWithdrawalWithDelayServiceTest {

    @InjectMocks CheckWithdrawalWithDelayService checkWithdrawalWithDelayService;

    @Mock LoadExchangeRequestCachePort loadExchangeRequestCachePort;
    @Mock PublishEventPort publishEventPort;
    @Mock ExchangeRequest exchange;

    CheckWithdrawalWithDelayCommand command
            = new CheckWithdrawalWithDelayCommand(EXCHANGE_ID, COUNT_ONE, DIRECTION_BUY);

    @Test
    @DisplayName("지연 후 출금 확인이 정상적으로 처리된다")
    void checkWithdrawalWithDelay_success() {
        // Given
        when(loadExchangeRequestCachePort.loadByExchangeId(EXCHANGE_ID))
                .thenReturn(Optional.of(exchange));

        // When
        checkWithdrawalWithDelayService.checkWithdrawalWithDelay(command);

        // Then
        verify(loadExchangeRequestCachePort).loadByExchangeId(EXCHANGE_ID);
        verify(exchange).markWithdrawalUnknown(COUNT_ONE);
        verify(publishEventPort).publish(exchange);
    }

    @Test
    @DisplayName("환전 요청이 없으면 예외가 발생한다")
    void checkWithdrawalWithDelay_exchangeRequest_not_found() {
        // Given
        when(loadExchangeRequestCachePort.loadByExchangeId(EXCHANGE_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ExchangeRequestNotFoundException.class,
                () -> checkWithdrawalWithDelayService.checkWithdrawalWithDelay(command));

        verify(loadExchangeRequestCachePort).loadByExchangeId(EXCHANGE_ID);
    }
} 