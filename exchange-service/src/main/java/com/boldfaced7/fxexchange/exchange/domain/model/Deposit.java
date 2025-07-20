package com.boldfaced7.fxexchange.exchange.domain.model;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.*;

import com.boldfaced7.fxexchange.exchange.domain.vo.deposit.DepositId;
import com.boldfaced7.fxexchange.exchange.domain.vo.deposit.Deposited;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.UserId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Deposit extends EventDomain {

    private DepositId depositId;
    private RequestId requestId;
    private ExchangeId exchangeId;
    private UserId userId;
    private Direction direction;
    private Deposited deposited;
    private LocalDateTime depositedAt;

    public static Deposit create(
            DepositId depositId,
            RequestId requestId,
            ExchangeId exchangeId,
            UserId userId,
            Direction direction,
            boolean success
    ) {
        return new Deposit(
                depositId,
                requestId,
                exchangeId,
                userId,
                direction,
                new Deposited(success),
                LocalDateTime.now()
        );
    }

    public static Deposit create(
            DepositId depositId,
            RequestId requestId,
            ExchangeId exchangeId,
            UserId userId,
            Direction direction,
            boolean success,
            LocalDateTime depositedAt
    ) {
        return new Deposit(
                depositId,
                requestId,
                exchangeId,
                userId,
                direction,
                new Deposited(success),
                depositedAt
        );
    }

    public boolean isSuccess() {
        return deposited.value();
    }

    public void processTransactionResult() {
        if (this.deposited.value()) {
            markDepositSucceeded();
        } else {
            markDepositFailed();
        }
    }

    public void processCheckResult() {
        if (this.deposited.value()) {
            markDepositSuccessChecked();
        } else {
            markDepositFailureChecked();
        }
    }

    /* 입금 관련 이벤트 생성 */

    // 성공
    private void markDepositSucceeded() {
        addEvent(new DepositSucceeded(requestId, exchangeId, depositId, direction));
    }

    // 실패
    private void markDepositFailed() {
        addEvent(new DepositFailed(requestId, exchangeId, direction));
    }

    // 성공 확인
    private void markDepositSuccessChecked() {
        addEvent(new DepositSuccessChecked(requestId, exchangeId, depositId, direction));
    }

    // 실패 확인
    private void markDepositFailureChecked() {
        addEvent(new DepositFailureChecked(requestId, exchangeId, direction));
    }
}
