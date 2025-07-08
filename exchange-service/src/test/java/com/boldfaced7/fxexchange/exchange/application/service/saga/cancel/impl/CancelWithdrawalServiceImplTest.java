package com.boldfaced7.fxexchange.exchange.application.service.saga.cancel.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.cancel.CancelWithdrawalPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.boldfaced7.fxexchange.exchange.application.util.TestConstraints.DIRECTION_BUY;
import static com.boldfaced7.fxexchange.exchange.application.util.TestConstraints.EXCHANGE_ID;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CancelWithdrawalServiceImplTest {

    @InjectMocks CancelWithdrawalServiceImpl cancelWithdrawalService;
    @Mock CancelWithdrawalPort cancelWithdrawalPort;

    @Test
    @DisplayName("출금 취소가 정상적으로 처리된다")
    void cancelWithdrawal_success() {
        // When
        cancelWithdrawalService.cancelWithdrawal(EXCHANGE_ID, DIRECTION_BUY);

        // Then
        verify(cancelWithdrawalPort).cancelWithdrawal(EXCHANGE_ID, DIRECTION_BUY);
    }
} 