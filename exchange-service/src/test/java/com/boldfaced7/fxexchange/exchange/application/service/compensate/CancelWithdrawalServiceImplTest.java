package com.boldfaced7.fxexchange.exchange.application.service.compensate;

import com.boldfaced7.fxexchange.exchange.application.port.out.CancelWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.service.compensate.impl.CancelWithdrawalServiceImpl;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CancelWithdrawalServiceImplTest {

    @InjectMocks
    private CancelWithdrawalServiceImpl cancelWithdrawalService;

    @Mock
    private CancelWithdrawalPort cancelWithdrawalPort;

    private ExchangeId exchangeId;

    @BeforeEach
    void setUp() {
        exchangeId = new ExchangeId("test-exchange-id");
    }

    @Test
    @DisplayName("출금 취소 요청을 전송해야 한다.")
    void cancelWithdrawal_Success() {
        // given
        doNothing().when(cancelWithdrawalPort).cancelWithdrawal(exchangeId, Direction.BUY);

        // when
        cancelWithdrawalService.cancelWithdrawal(exchangeId, Direction.BUY);

        // then
        verify(cancelWithdrawalPort).cancelWithdrawal(exchangeId, Direction.BUY);
    }
} 