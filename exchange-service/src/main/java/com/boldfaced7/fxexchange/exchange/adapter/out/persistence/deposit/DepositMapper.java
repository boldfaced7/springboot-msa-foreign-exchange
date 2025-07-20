package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.deposit;

import com.boldfaced7.fxexchange.exchange.domain.model.Deposit;
import com.boldfaced7.fxexchange.exchange.domain.vo.deposit.DepositId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.UserId;

public class DepositMapper {
    public static JpaDeposit toJpa(Deposit deposit) {
        return new JpaDeposit(
                deposit.getDepositId().value(),
                deposit.getRequestId().value(),
                deposit.getExchangeId().value(),
                deposit.getUserId().value(),
                deposit.getDirection(),
                deposit.isSuccess(),
                deposit.getDepositedAt()
        );
    }

    public static Deposit toDomain(JpaDeposit jpaDeposit) {
        return Deposit.create(
                new DepositId(jpaDeposit.getDepositId()),
                new RequestId(jpaDeposit.getRequestId()),
                new ExchangeId(jpaDeposit.getExchangeId()),
                new UserId(jpaDeposit.getUserId()),
                jpaDeposit.getDirection(),
                jpaDeposit.isSuccess(),
                jpaDeposit.getDepositedAt()
        );
    }
}
