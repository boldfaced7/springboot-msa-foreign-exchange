package com.boldfaced7.fxexchange.exchange.application.service.buy.compensate;

import com.boldfaced7.fxexchange.exchange.application.port.out.buy.UndoKrwWithdrawalPort;
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
class CancelKrwWithdrawalServiceImplTest {

    @InjectMocks
    private CancelKrwWithdrawalServiceImpl cancelKrwWithdrawalService;

    @Mock
    private UndoKrwWithdrawalPort undoKrwWithdrawalPort;

    private ExchangeId exchangeId;

    @BeforeEach
    void setUp() {
        exchangeId = new ExchangeId("test-exchange-id");
    }

    @Test
    @DisplayName("원화 출금 취소 요청을 전송해야 한다.")
    void cancelKrwWithdrawal_Success() {
        // given
        doNothing().when(undoKrwWithdrawalPort).undoKrwWithdrawn(exchangeId);

        // when
        cancelKrwWithdrawalService.cancelKrwWithdrawal(exchangeId);

        // then
        verify(undoKrwWithdrawalPort).undoKrwWithdrawn(exchangeId);
    }
} 