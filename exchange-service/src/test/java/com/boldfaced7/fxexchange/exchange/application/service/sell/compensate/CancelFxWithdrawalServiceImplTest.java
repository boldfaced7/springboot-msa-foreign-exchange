package com.boldfaced7.fxexchange.exchange.application.service.sell.compensate;

import com.boldfaced7.fxexchange.exchange.application.port.out.sell.UndoFxWithdrawalPort;
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
class CancelFxWithdrawalServiceImplTest {

    @InjectMocks
    private CancelFxWithdrawalServiceImpl cancelFxWithdrawalService;

    @Mock
    private UndoFxWithdrawalPort undoFxWithdrawalPort;

    private ExchangeId exchangeId;

    @BeforeEach
    void setUp() {
        exchangeId = new ExchangeId("test-exchange-id");
    }

    @Test
    @DisplayName("외화 출금 취소 요청을 전송해야 한다.")
    void cancelFxWithdrawal_Success() {
        // given
        doNothing().when(undoFxWithdrawalPort).undoFxWithdrawal(exchangeId);

        // when
        cancelFxWithdrawalService.cancelFxWithdrawal(exchangeId);

        // then
        verify(undoFxWithdrawalPort).undoFxWithdrawal(exchangeId);
    }
} 