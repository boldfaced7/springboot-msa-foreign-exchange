package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.model.Withdrawal;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import com.boldfaced7.fxexchange.exchange.domain.vo.UserId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalId;

public class WithdrawalMapper {
    public static WithdrawalJpa toJpa(Withdrawal withdrawal) {
        return new WithdrawalJpa(
                withdrawal.getWithdrawalId().value(),
                withdrawal.getRequestId().value(),
                withdrawal.getExchangeId().value(),
                withdrawal.getUserId().value(),
                withdrawal.getDirection(),
                withdrawal.isSuccess(),
                withdrawal.getWithdrawnAt()
        );
    }
    
    public static Withdrawal toDomain(WithdrawalJpa withdrawalJpa) {
        return Withdrawal.create(
                new WithdrawalId(withdrawalJpa.getWithdrawalId()),
                new RequestId(withdrawalJpa.getRequestId()),
                new ExchangeId(withdrawalJpa.getExchangeId()),
                new UserId(withdrawalJpa.getUserId()),
                withdrawalJpa.getDirection(),
                withdrawalJpa.isSuccess(),
                withdrawalJpa.getWithdrawnAt()
        );
    }
}
