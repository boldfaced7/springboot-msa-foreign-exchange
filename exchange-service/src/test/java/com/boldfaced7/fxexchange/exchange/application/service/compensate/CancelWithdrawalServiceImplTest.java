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

import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelWithdrawalServiceImplTest {

    @InjectMocks
    private CancelWithdrawalServiceImpl cancelWithdrawalService;

    @Mock
    private Map<Direction, CancelWithdrawalPort> undoWithdrawalPorts;

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
        doNothing().when(cancelWithdrawalPort).cancelWithdrawal(exchangeId);
        when(undoWithdrawalPorts.get(Direction.BUY)).thenReturn(cancelWithdrawalPort);

        // when
        cancelWithdrawalService.cancelWithdrawal(exchangeId, Direction.BUY);

        // then
        verify(undoWithdrawalPorts).get(Direction.BUY);
        verify(cancelWithdrawalPort).cancelWithdrawal(exchangeId);
    }
} 