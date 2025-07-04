package com.boldfaced7.fxexchange.exchange.application.saga;

import com.boldfaced7.fxexchange.common.SagaOrchestrator;
import com.boldfaced7.fxexchange.exchange.application.service.compensate.CancelWithdrawalService;
import com.boldfaced7.fxexchange.exchange.application.service.deposit.CheckDepositService;
import com.boldfaced7.fxexchange.exchange.application.service.deposit.DepositService;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater;
import com.boldfaced7.fxexchange.exchange.application.service.util.TransactionCheckDelayer;
import com.boldfaced7.fxexchange.exchange.application.service.util.WarningMessageSender;
import com.boldfaced7.fxexchange.exchange.application.service.withdrawal.CheckWithdrawalService;
import com.boldfaced7.fxexchange.exchange.application.service.withdrawal.WithdrawService;
import com.boldfaced7.fxexchange.exchange.domain.enums.TransactionCheckType;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.*;
import com.boldfaced7.fxexchange.exchange.domain.event.request.ExchangeCurrencyFailed;
import com.boldfaced7.fxexchange.exchange.domain.event.request.ExchangeCurrencyStarted;
import com.boldfaced7.fxexchange.exchange.domain.event.request.ExchangeCurrencySucceeded;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.*;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@SagaOrchestrator
@RequiredArgsConstructor
public class ExchangeCurrencySagaOrchestratorImpl implements ExchangeCurrencySagaOrchestrator {

    @Value("${exchange.check.max-count:3}")
    private int checkMaxCount;

    private final WithdrawService withdrawService;
    private final CheckWithdrawalService checkWithdrawalService;

    private final DepositService depositService;
    private final CheckDepositService checkDepositService;

    private final CancelWithdrawalService cancelWithdrawalService;

    private final ExchangeEventPublisher exchangeEventPublisher;
    private final ExchangeRequestUpdater exchangeRequestUpdater;
    private final TransactionCheckDelayer transactionCheckDelayer;
    private final WarningMessageSender warningMessageSender;

    @Override
    public ExchangeDetail startExchange(ExchangeRequest requested) {
        var withdrawn = withdrawService.withdraw(requested);
        var deposited = depositService.deposit(withdrawn.exchangeRequest());
        return new ExchangeDetail(withdrawn, deposited);
    }

    /* 환전 관련 이벤트 처리 */

    // 환전 시작
    @Override
    @Async
    @EventListener
    public void handle(ExchangeCurrencyStarted event) {
    }

    // 환전 성공
    @Override
    @Async
    @EventListener
    public void handle(ExchangeCurrencySucceeded event) {
        // 환전 요청 종결
        exchangeRequestUpdater.update(
                event.requestId(),
                ExchangeRequest::terminate
        );
    }

    // 환전 실패
    @Override
    @Async
    @EventListener
    public void handle(ExchangeCurrencyFailed event) {
        // 환전 요청 종결
        exchangeRequestUpdater.update(
                event.requestId(),
                ExchangeRequest::terminate
        );
    }

    /* 출금 관련 이벤트 처리 */

    // 출금: 성공
    @Override
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(WithdrawalSucceeded event) {

    }

    // 출금: 실패
    @Override
    @Async
    @EventListener
    public void handle(WithdrawalFailed event) {
        // 환전 실패 이벤트 발행
        exchangeEventPublisher.publishEvents(
                event.requestId(),
                ExchangeRequest::exchangeCurrencyFailed
        );
    }

    // 출금: 결과 알 수 없음
    @Override
    @Async
    @EventListener
    public void handle(WithdrawalResultUnknown event) {
        if (event.count().isEqualOrSmallerThan(checkMaxCount)) {
            // 출금 결과 확인
            checkWithdrawalService.checkWithdrawal(
                    event.requestId(),
                    event.count(),
                    event.direction()
            );
        } else {
            // 경고 메시지 발송
            warningMessageSender.sendWarningMessage(
                    event.requestId(),
                    event.exchangeId()
            );
        }
    }

    // 출금: 취소됨
    @Override
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(WithdrawalCancelled event) {
        // 환전 실패 이벤트 발행
        exchangeEventPublisher.publishEvents(
                event.requestId(),
                ExchangeRequest::exchangeCurrencyFailed
        );
    }

    /* 출금 확인 관련 이벤트 처리 */

    // 출금 확인: 성공 확인됨
    @Override
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(WithdrawalSuccessChecked event) {
        // 출금 취소
        cancelWithdrawalService.cancelWithdrawal(
                event.exchangeId(),
                event.direction()
        );
    }

    // 출금 확인: 실패 확인됨
    @Override
    @Async
    @EventListener
    public void handle(WithdrawalFailureChecked event) {
        // 환전 실패 이벤트 발행
        exchangeEventPublisher.publishEvents(
                event.requestId(),
                ExchangeRequest::exchangeCurrencyFailed
        );
    }

    // 출금 확인: 결과 알 수 없음
    @Override
    @Async
    @EventListener
    public void handle(WithdrawalCheckUnknown event) {
        // 출금 확인 지연
        transactionCheckDelayer.delayTransactionCheck(
                event.exchangeId(),
                event.count(),
                event.direction(),
                TransactionCheckType.fromEventClass(event.getClass())
        );
    }

    /* 입금 관련 이벤트 처리 */

    // 입금: 성공
    @Override
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DepositSucceeded event) {
        // 환전 성공 이벤트 발행
        exchangeEventPublisher.publishEvents(
                event.requestId(),
                ExchangeRequest::exchangeCurrencySucceeded
        );
    }

    // 입금: 실패
    @Override
    @Async
    @EventListener
    public void handle(DepositFailed event) {
        // 출금 취소
        cancelWithdrawalService.cancelWithdrawal(
                event.exchangeId(),
                event.direction()
        );
    }

    // 입금: 결과 알 수 없음
    @Override
    @Async
    @EventListener
    public void handle(DepositResultUnknown event) {
        if (event.count().isEqualOrSmallerThan(checkMaxCount)) {
            // 입금 결과 확인
            checkDepositService.checkDeposit(
                    event.requestId(),
                    event.count(),
                    event.direction()
            );
        } else {
            // 경고 메시지 발송
            warningMessageSender.sendWarningMessage(
                    event.requestId(),
                    event.exchangeId()
            );
        }
    }

    /* 입금 확인 관련 이벤트 처리 */

    // 입금 확인: 성공 확인됨
    @Override
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DepositSuccessChecked event) {
        // 환전 성공 이벤트 발행
        exchangeEventPublisher.publishEvents(
                event.requestId(),
                ExchangeRequest::exchangeCurrencySucceeded
        );
    }

    // 입금 확인: 실패 확인됨
    @Override
    @Async
    @EventListener
    public void handle(DepositFailureChecked event) {
        // 출금 취소
        cancelWithdrawalService.cancelWithdrawal(
                event.exchangeId(),
                event.direction()
        );
    }

    // 입금 확인: 결과 알 수 없음
    @Override
    @Async
    @EventListener
    public void handle(DepositCheckUnknown event) {
        // 입금 확인 지연
        transactionCheckDelayer.delayTransactionCheck(
                event.exchangeId(),
                event.count(),
                event.direction(),
                TransactionCheckType.fromEventClass(event.getClass())
        );
    }

}
