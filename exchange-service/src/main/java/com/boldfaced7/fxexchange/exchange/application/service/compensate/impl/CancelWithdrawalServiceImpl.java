package com.boldfaced7.fxexchange.exchange.application.service.compensate.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.UndoWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.service.compensate.CancelWithdrawalService;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CancelWithdrawalServiceImpl implements CancelWithdrawalService {

    private final Map<Direction, UndoWithdrawalPort> undoWithdrawalPorts;

    @Override
    public void cancelWithdrawal(ExchangeId exchangeId, Direction direction) {
        undoWithdrawalPorts.get(direction).undoWithdrawal(exchangeId);
    }
}
