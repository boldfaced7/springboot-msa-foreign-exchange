package com.boldfaced7.fxexchange.exchange.application.service.saga.cancel.impl;

import com.boldfaced7.fxexchange.exchange.application.port.aop.Idempotent;
import com.boldfaced7.fxexchange.exchange.application.port.out.cancel.CancelWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.service.saga.cancel.CancelWithdrawalService;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelWithdrawalServiceImpl implements CancelWithdrawalService {

    private final CancelWithdrawalPort cancelWithdrawalPort;

    @Override
    @Idempotent(prefix = "cancel-withdrawal:request:", key = "#exchangeId")
    public void cancelWithdrawal(ExchangeId exchangeId, Direction direction) {
        cancelWithdrawalPort.cancelWithdrawal(exchangeId, direction);
    }
}
