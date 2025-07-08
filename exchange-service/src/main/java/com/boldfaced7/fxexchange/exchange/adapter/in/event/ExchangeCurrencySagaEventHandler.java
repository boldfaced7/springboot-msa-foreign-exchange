package com.boldfaced7.fxexchange.exchange.adapter.in.event;

import com.boldfaced7.fxexchange.exchange.application.service.saga.ExchangeCurrencySagaOrchestrator;
import com.boldfaced7.fxexchange.exchange.domain.event.cancel.WithdrawalCancelSucceeded;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.*;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeCurrencySagaEventHandler {

    private final ExchangeCurrencySagaOrchestrator exchangeCurrencySagaOrchestrator;

    /* 출금 관련 이벤트 처리 */

    // 출금: 실패
    @Async
    @EventListener
    public void handle(WithdrawalFailed event) {
        exchangeCurrencySagaOrchestrator.handle(event);
    }

    // 출금: 결과 알 수 없음
    @Async
    @EventListener
    public void handle(WithdrawalUnknown event) {
        exchangeCurrencySagaOrchestrator.handle(event);
    }

    /* 출금 확인 관련 이벤트 처리 */

    // 출금 확인: 성공 확인됨
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(WithdrawalSuccessChecked event) {
        exchangeCurrencySagaOrchestrator.handle(event);
    }

    // 출금 확인: 실패 확인됨
    @Async
    @EventListener
    public void handle(WithdrawalFailureChecked event) {
        exchangeCurrencySagaOrchestrator.handle(event);
    }

    // 출금 확인: 결과 알 수 없음
    @Async
    @EventListener
    public void handle(WithdrawalCheckUnknown event) {
        exchangeCurrencySagaOrchestrator.handle(event);
    }

    // 출금 확인: 확인 횟수 초과
    @Async
    @EventListener
    public void handle(WithdrawalAttemptExhausted event) {
        exchangeCurrencySagaOrchestrator.handle(event);
    }


    /* 입금 관련 이벤트 처리 */

    // 입금: 실패
    @Async
    @EventListener
    public void handle(DepositFailed event) {
        exchangeCurrencySagaOrchestrator.handle(event);
    }

    // 입금: 결과 알 수 없음
    @Async
    @EventListener
    public void handle(DepositUnknown event) {
        exchangeCurrencySagaOrchestrator.handle(event);
    }

    /* 입금 확인 관련 이벤트 처리 */

    // 입금 확인: 성공 확인됨
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DepositSuccessChecked event) {
        exchangeCurrencySagaOrchestrator.handle(event);
    }

    // 입금 확인: 실패 확인됨
    @Async
    @EventListener
    public void handle(DepositFailureChecked event) {
        exchangeCurrencySagaOrchestrator.handle(event);
    }

    // 입금 확인: 결과 알 수 없음
    @Async
    @EventListener
    public void handle(DepositCheckUnknown event) {
        exchangeCurrencySagaOrchestrator.handle(event);
    }

    // 입금 확인: 확인 횟수 초과
    @Async
    @EventListener
    public void handle(DepositAttemptExhausted event) {
        exchangeCurrencySagaOrchestrator.handle(event);
    }


    /* 출금 취소 관련 이벤트 처리 */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(WithdrawalCancelSucceeded event) {
        exchangeCurrencySagaOrchestrator.handle(event);
    }
}
