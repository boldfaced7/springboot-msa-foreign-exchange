package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.cancel;

import com.boldfaced7.fxexchange.exchange.domain.model.WithdrawalCancel;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.UserId;
import com.boldfaced7.fxexchange.exchange.domain.vo.cancel.WithdrawalCancelId;

public class WithdrawalCancelMapper {
    public static JpaWithdrawalCancel toJpa(WithdrawalCancel withdrawalCancel) {
        return new JpaWithdrawalCancel(
                withdrawalCancel.getWithdrawalCancelId().value(),
                withdrawalCancel.getRequestId().value(),
                withdrawalCancel.getExchangeId().value(),
                withdrawalCancel.getUserId().value(),
                withdrawalCancel.getDirection(),
                withdrawalCancel.isSuccess(),
                withdrawalCancel.getCancelledAt()
        );
    }

    public static WithdrawalCancel toDomain(JpaWithdrawalCancel jpaWithdrawalCancel) {
        return WithdrawalCancel.create(
                new WithdrawalCancelId(jpaWithdrawalCancel.getWithdrawalCancelId()),
                new RequestId(jpaWithdrawalCancel.getRequestId()),
                new ExchangeId(jpaWithdrawalCancel.getExchangeId()),
                new UserId(jpaWithdrawalCancel.getUserId()),
                jpaWithdrawalCancel.getDirection(),
                jpaWithdrawalCancel.isSuccess(),
                jpaWithdrawalCancel.getCancelledAt()
        );
    }

}
