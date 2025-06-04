package com.boldfaced7.fxexchange.exchange.application.service.deposit;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public interface CheckDepositService {
    void checkDeposit(RequestId requestId, Count count, Direction direction);
}
