package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.deposit;

import com.boldfaced7.fxexchange.exchange.domain.model.Deposit;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositId;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import com.boldfaced7.fxexchange.exchange.domain.vo.UserId;

public class DepositMapper {
    public static DepositJpa toJpa(Deposit deposit) {
        return new DepositJpa(
                deposit.getDepositId().value(),
                deposit.getRequestId().value(),
                deposit.getExchangeId().value(),
                deposit.getUserId().value(),
                deposit.getDirection(),
                deposit.isSuccess(),
                deposit.getDepositedAt()
        );
    }

    public static Deposit toDomain(DepositJpa depositJpa) {
        return Deposit.create(
                new DepositId(depositJpa.getDepositId()),
                new RequestId(depositJpa.getRequestId()),
                new ExchangeId(depositJpa.getExchangeId()),
                new UserId(depositJpa.getUserId()),
                depositJpa.getDirection(),
                depositJpa.isSuccess(),
                depositJpa.getDepositedAt()
        );
    }
}
