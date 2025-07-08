package com.boldfaced7.fxexchange.exchange.domain.model;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalFailed;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalFailureChecked;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalSucceeded;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalSuccessChecked;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Withdrawal extends EventDomain {
    private WithdrawalId withdrawalId;
    private RequestId requestId;
    private ExchangeId exchangeId;
    private UserId userId;
    private Direction direction;
    private Withdrawn withdrawn;
    private LocalDateTime withdrawnAt;

    public static Withdrawal create(
            WithdrawalId withdrawalId,
            RequestId requestId,
            ExchangeId exchangeId,
            UserId userId,
            Direction direction,
            boolean success
    ) {
        return new Withdrawal(
                withdrawalId,
                requestId,
                exchangeId,
                userId,
                direction,
                new Withdrawn(success),
                LocalDateTime.now()
        );
    }

    public static Withdrawal create(
            WithdrawalId withdrawalId,
            RequestId requestId,
            ExchangeId exchangeId,
            UserId userId,
            Direction direction,
            boolean success,
            LocalDateTime withdrawnAt
    ) {
        return new Withdrawal(
                withdrawalId,
                requestId,
                exchangeId,
                userId,
                direction,
                new Withdrawn(success),
                withdrawnAt
        );
    }

    public boolean isSuccess() {
        return withdrawn.value();
    }

    public void processTransactionResult() {
        if (this.withdrawn.value()) {
            markWithdrawalSucceeded();
        } else {
            markWithdrawalFailed();
        }
    }

    public void processCheckResult() {
        if (this.withdrawn.value()) {
            markWithdrawalSuccessChecked();
        } else {
            markWithdrawalFailureChecked();
        }
    }

    // 성공
    private void markWithdrawalSucceeded() {
        addEvent(new WithdrawalSucceeded(requestId, exchangeId, withdrawalId, direction));
    }

    // 실패
    private void markWithdrawalFailed() {
        addEvent(new WithdrawalFailed(requestId, exchangeId, direction));
    }

    // 성공 확인
    private void markWithdrawalSuccessChecked() {
        addEvent(new WithdrawalSuccessChecked(requestId, exchangeId, withdrawalId, direction));
    }
        
    // 실패 확인
    private void markWithdrawalFailureChecked() {
        addEvent(new WithdrawalFailureChecked(requestId, exchangeId, direction));
    }

}
