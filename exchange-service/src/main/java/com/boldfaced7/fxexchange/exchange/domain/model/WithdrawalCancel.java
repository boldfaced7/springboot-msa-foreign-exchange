package com.boldfaced7.fxexchange.exchange.domain.model;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.event.cancel.WithdrawalCancelSucceeded;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WithdrawalCancel extends EventDomain {

    private WithdrawalCancelId withdrawalCancelId;
    private RequestId requestId;
    private ExchangeId exchangeId;
    private UserId userId;
    private Direction direction;
    private WithdrawalCancelled withdrawalCancelled;
    private LocalDateTime cancelledAt;

    public static WithdrawalCancel create(
        WithdrawalCancelId withdrawalCancelId,
        RequestId requestId,
        ExchangeId exchangeId,
        UserId userId,
        Direction direction,
        boolean success
    ) {
        WithdrawalCancel withdrawalCancel = new WithdrawalCancel(
                withdrawalCancelId,
                requestId,
                exchangeId,
                userId,
                direction,
                new WithdrawalCancelled(success),
                LocalDateTime.now()
        );
        withdrawalCancel.markWithdrawalCancelSucceeded();
        return withdrawalCancel;
    }

    public static WithdrawalCancel create(
            WithdrawalCancelId withdrawalCancelId,
            RequestId requestId,
            ExchangeId exchangeId,
            UserId userId,
            Direction direction,
            boolean success,
            LocalDateTime cancelledAt
    ) {
        return new WithdrawalCancel(
                withdrawalCancelId,
                requestId,
                exchangeId,
                userId,
                direction,
                new WithdrawalCancelled(success),
                cancelledAt
        );
    }

    public boolean isSuccess() {
        return withdrawalCancelled.value();
    }

    // 출금: 취소됨
    private void markWithdrawalCancelSucceeded() {
        addEvent(new WithdrawalCancelSucceeded(requestId, exchangeId, withdrawalCancelId, direction));
    }

}
