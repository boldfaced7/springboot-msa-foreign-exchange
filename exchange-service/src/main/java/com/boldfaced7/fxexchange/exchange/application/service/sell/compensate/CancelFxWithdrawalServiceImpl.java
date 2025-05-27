package com.boldfaced7.fxexchange.exchange.application.service.sell.compensate;

import com.boldfaced7.fxexchange.exchange.application.port.out.sell.UndoFxWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelFxWithdrawalServiceImpl implements CancelFxWithdrawalService {

    private final UndoFxWithdrawalPort undoFxWithdrawalPort;

    @Override
    public void cancelFxWithdrawal(ExchangeId exchangeId) {
        undoFxWithdrawalPort.undoFxWithdrawal(exchangeId);
    }
}
