package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.model.Withdrawal;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.UserId;
import com.boldfaced7.fxexchange.exchange.domain.vo.withdrawal.WithdrawalId;

public class WithdrawalMapper {
    public static JpaWithdrawal toJpa(Withdrawal withdrawal) {
        return new JpaWithdrawal(
                withdrawal.getWithdrawalId().value(),
                withdrawal.getRequestId().value(),
                withdrawal.getExchangeId().value(),
                withdrawal.getUserId().value(),
                withdrawal.getDirection(),
                withdrawal.isSuccess(),
                withdrawal.getWithdrawnAt()
        );
    }
    
    public static Withdrawal toDomain(JpaWithdrawal jpaWithdrawal) {
        return Withdrawal.create(
                new WithdrawalId(jpaWithdrawal.getWithdrawalId()),
                new RequestId(jpaWithdrawal.getRequestId()),
                new ExchangeId(jpaWithdrawal.getExchangeId()),
                new UserId(jpaWithdrawal.getUserId()),
                jpaWithdrawal.getDirection(),
                jpaWithdrawal.isSuccess(),
                jpaWithdrawal.getWithdrawnAt()
        );
    }
}
