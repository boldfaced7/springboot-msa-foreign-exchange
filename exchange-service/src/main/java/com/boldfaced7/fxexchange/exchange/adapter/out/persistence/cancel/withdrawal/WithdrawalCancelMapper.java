package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.cancel.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.model.WithdrawalCancel;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import com.boldfaced7.fxexchange.exchange.domain.vo.UserId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalCancelId;

public class WithdrawalCancelMapper {
    public static WithdrawalCancelJpa toJpa(WithdrawalCancel withdrawalCancel) {
        return new WithdrawalCancelJpa(
                withdrawalCancel.getWithdrawalCancelId().value(),
                withdrawalCancel.getRequestId().value(),
                withdrawalCancel.getExchangeId().value(),
                withdrawalCancel.getUserId().value(),
                withdrawalCancel.getDirection(),
                withdrawalCancel.isSuccess(),
                withdrawalCancel.getCancelledAt()
        );
    }

    public static WithdrawalCancel toDomain(WithdrawalCancelJpa withdrawalCancelJpa) {
        return WithdrawalCancel.create(
                new WithdrawalCancelId(withdrawalCancelJpa.getWithdrawalCancelId()),
                new RequestId(withdrawalCancelJpa.getRequestId()),
                new ExchangeId(withdrawalCancelJpa.getExchangeId()),
                new UserId(withdrawalCancelJpa.getUserId()),
                withdrawalCancelJpa.getDirection(),
                withdrawalCancelJpa.isSuccess(),
                withdrawalCancelJpa.getCancelledAt()
        );
    }

}
