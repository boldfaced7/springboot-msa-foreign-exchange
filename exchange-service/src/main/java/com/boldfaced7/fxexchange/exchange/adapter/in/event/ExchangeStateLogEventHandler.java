package com.boldfaced7.fxexchange.exchange.adapter.in.event;

import com.boldfaced7.fxexchange.exchange.application.port.in.LogExchangeStateCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.LogExchangeStateUseCase;
import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.DepositFailed;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.DepositFailureChecked;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.DepositUnknown;
import com.boldfaced7.fxexchange.exchange.domain.event.exchange.ExchangeCurrencyFailed;
import com.boldfaced7.fxexchange.exchange.domain.event.exchange.ExchangeCurrencyStarted;
import com.boldfaced7.fxexchange.exchange.domain.event.exchange.ExchangeCurrencySucceeded;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalSucceeded;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalSuccessChecked;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalUnknown;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ExchangeStateLogEventHandler {

    private final LogExchangeStateUseCase logExchangeStateUseCase;

    /* 환전 관련 이벤트 처리 */
    // 환전 시작
    @Async
    @EventListener
    public void handle(ExchangeCurrencyStarted event) {
        logExchangeStateUseCase.logExchangeCurrencyStarted(toCommand(event));
    }

    // 환전 성공
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ExchangeCurrencySucceeded event) {
        logExchangeStateUseCase.logExchangeCurrencySucceeded(toCommand(event));
    }

    // 환전 실패
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ExchangeCurrencyFailed event) {
        logExchangeStateUseCase.logExchangeCurrencyFailed(toCommand(event));
    }

    /* 출금 관련 이벤트 처리 */
    // 출금 성공
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(WithdrawalSucceeded event) {
        logExchangeStateUseCase.logWithdrawalSucceeded(toCommand(event));
    }

    // 출금 결과 알 수 없음
    @Async
    @EventListener
    public void handle(WithdrawalUnknown event) {
        if (event.count().value() == 0) {
            logExchangeStateUseCase.logWithdrawalUnknown(toCommand(event));
        }
    }

    // 출금 성공 확인됨
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(WithdrawalSuccessChecked event) {
        logExchangeStateUseCase.logWithdrawalSuccessChecked(toCommand(event));
    }

    /* 입금 관련 이벤트 처리 */
    // 입금 실패
    @Async
    @EventListener
    public void handle(DepositFailed event) {
        logExchangeStateUseCase.logDepositFailed(toCommand(event));
    }

    // 입금 결과 알 수 없음
    @Async
    @EventListener
    public void handle(DepositUnknown event) {
        if (event.count().value() == 0) {
            logExchangeStateUseCase.logDepositUnknown(toCommand(event));
        }
    }

    // 입금 실패 확인됨
    @Async
    @EventListener
    public void handle(DepositFailureChecked event) {
        logExchangeStateUseCase.logDepositFailureChecked(toCommand(event));
    }

    private LogExchangeStateCommand toCommand(DomainEvent event) {
        return new LogExchangeStateCommand(
                event.requestId(),
                event.direction(),
                event.raisedAt()
        );
    }

}
