package com.boldfaced7.fxexchange.exchange.application.service.buy.compensate;

import com.boldfaced7.fxexchange.exchange.application.port.out.buy.UndoKrwWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelKrwWithdrawalServiceImpl implements CancelKrwWithdrawalService {

    private final UndoKrwWithdrawalPort undoKrwWithdrawalPort;

    @Override
    public void cancelKrwWithdrawal(ExchangeId exchangeId) {
        undoKrwWithdrawalPort.undoKrwWithdrawn(exchangeId);
    }
}
