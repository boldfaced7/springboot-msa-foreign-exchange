package com.boldfaced7.fxexchange.exchange.application.log;

import com.boldfaced7.fxexchange.exchange.application.port.out.SaveExchangeStateLogPort;
import com.boldfaced7.fxexchange.exchange.domain.enums.ExchangeState;
import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.DepositFailed;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.DepositFailureChecked;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.DepositResultUnknown;
import com.boldfaced7.fxexchange.exchange.domain.event.request.ExchangeCurrencyFailed;
import com.boldfaced7.fxexchange.exchange.domain.event.request.ExchangeCurrencyStarted;
import com.boldfaced7.fxexchange.exchange.domain.event.request.ExchangeCurrencySucceeded;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalResultUnknown;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalSucceeded;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalSuccessChecked;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeStateLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ExchangeStateLogger {

    private final SaveExchangeStateLogPort saveExchangeStateLogPort;

    /* 환전 관련 이벤트 처리 */
    // 환전 시작
    @Async
    @EventListener
    public void handle(ExchangeCurrencyStarted event) {
        saveEvent(event);
    }

    // 환전 성공
    @Async
    @EventListener
    public void handle(ExchangeCurrencySucceeded event) {
        saveEvent(event);
    }

    // 환전 실패
    @Async
    @EventListener
    public void handle(ExchangeCurrencyFailed event) {
        saveEvent(event);
    }

    /* 출금 관련 이벤트 처리 */
    // 출금 성공
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(WithdrawalSucceeded event) {
        saveEvent(event);
    }

    // 출금 결과 알 수 없음
    @Async
    @EventListener
    public void handle(WithdrawalResultUnknown event) {
        if (event.count().value() == 0) {
            saveEvent(event);
        }
    }

    // 출금 성공 확인됨
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(WithdrawalSuccessChecked event) {
        saveEvent(event);
    }

    /* 입금 관련 이벤트 처리 */
    // 입금 실패
    @Async
    @EventListener
    public void handle(DepositFailed event) {
        saveEvent(event);
    }

    // 입금 결과 알 수 없음
    @Async
    @EventListener
    public void handle(DepositResultUnknown event) {
        if (event.count().value() == 0) {
            saveEvent(event);
        }
    }


    // 입금 실패 확인됨
    @Async
    @EventListener
    public void handle(DepositFailureChecked event) {
        saveEvent(event);
    }


    private void saveEvent(DomainEvent event) {
        ExchangeStateLog exchangeStateLog = toModel(event);
        saveExchangeStateLogPort.save(exchangeStateLog);
    }

    ExchangeStateLog toModel(DomainEvent event) {
        return ExchangeStateLog.of(
                event.requestId(),
                event.direction(),
                ExchangeState.fromEventClass(event.getClass()),
                event.raisedAt()
        );
    }
}
