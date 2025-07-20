package com.boldfaced7.fxexchange.exchange.application.service.saga;

import com.boldfaced7.fxexchange.common.SagaOrchestrator;
import com.boldfaced7.fxexchange.exchange.application.port.out.external.ScheduleCheckRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.external.SendWarningMessagePort;
import com.boldfaced7.fxexchange.exchange.application.service.saga.cancel.CancelWithdrawalService;
import com.boldfaced7.fxexchange.exchange.application.service.saga.deposit.CheckDepositService;
import com.boldfaced7.fxexchange.exchange.application.service.saga.deposit.DepositService;
import com.boldfaced7.fxexchange.exchange.application.service.saga.exchange.CompleteExchangeService;
import com.boldfaced7.fxexchange.exchange.application.service.saga.exchange.CreateExchangeRequestService;
import com.boldfaced7.fxexchange.exchange.application.service.saga.withdrawal.CheckWithdrawalService;
import com.boldfaced7.fxexchange.exchange.application.service.saga.withdrawal.WithdrawService;
import com.boldfaced7.fxexchange.exchange.domain.enums.TransactionType;
import com.boldfaced7.fxexchange.exchange.domain.event.cancel.WithdrawalCancelSucceeded;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.*;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.*;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SagaOrchestrator
@RequiredArgsConstructor
public class ExchangeCurrencySagaOrchestratorImpl implements ExchangeCurrencySagaOrchestrator {

    // 환전
    private final CreateExchangeRequestService createExchangeRequestService;
    private final CompleteExchangeService completeExchangeRequestService;

    // 출금
    private final WithdrawService withdrawService;
    private final CheckWithdrawalService checkWithdrawalService;

    // 입금
    private final DepositService depositService;
    private final CheckDepositService checkDepositService;

    // 출금 취소
    private final CancelWithdrawalService cancelWithdrawalService;

    private final ScheduleCheckRequestPort scheduleCheckRequestPort;
    private final SendWarningMessagePort sendWarningMessagePort;


    @Override
    public ExchangeDetail startExchange(ExchangeRequest toBeRequested) {
        var requested = createExchangeRequestService.createExchangeRequest(toBeRequested);
        var withdrawn = withdrawService.withdraw(requested);
        var deposited = depositService.deposit(requested);
        var exchanged = completeExchangeRequestService.succeedExchange(requested);
        return new ExchangeDetail(withdrawn, deposited, exchanged);
    }

    /* 출금 관련 이벤트 처리 */

    // 출금: 실패
    @Override
    public void handle(WithdrawalFailed event) {
        // 환전 종료 처리
        completeExchangeRequestService.failExchange(
                event.requestId()
        );
    }

    // 출금: 결과 알 수 없음
    @Override
    public void handle(WithdrawalUnknown event) {
        // 출금 확인
        checkWithdrawalService.checkWithdrawal(
                event.requestId(),
                event.count()
        );
    }

    /* 출금 확인 관련 이벤트 처리 */

    // 출금 확인: 성공 확인됨
    @Override
    public void handle(WithdrawalSuccessChecked event) {
        // 출금 취소
        cancelWithdrawalService.cancelWithdrawal(
                event.exchangeId(),
                event.direction()
        );
    }

    // 출금 확인: 실패 확인됨
    @Override
    public void handle(WithdrawalFailureChecked event) {
        // 환전 종료 처리
        completeExchangeRequestService.failExchange(
                event.requestId()
        );
    }

    // 출금 확인: 결과 알 수 없음
    @Override
    public void handle(WithdrawalCheckUnknown event) {
        // 출금 확인 지연
        scheduleCheckRequestPort.scheduleCheckRequest(
                event.exchangeId(),
                event.delay(),
                event.count(),
                event.direction(),
                TransactionType.WITHDRAWAL
        );
    }

    // 출금 확인: 확인 횟수 초과
    @Override
    public void handle(WithdrawalAttemptExhausted event) {
        // 경고 메시지 발송
        sendWarningMessagePort.sendWarningMessage(
                event.requestId(),
                event.exchangeId()
        );
    }

    /* 입금 관련 이벤트 처리 */

    // 입금: 실패
    @Override
    public void handle(DepositFailed event) {
        // 출금 취소
        cancelWithdrawalService.cancelWithdrawal(
                event.exchangeId(),
                event.direction()
        );
    }

    // 입금: 결과 알 수 없음
    @Override
    public void handle(DepositUnknown event) {
        // 입금 결과 확인
        checkDepositService.checkDeposit(
                event.requestId(),
                event.count()
        );
    }

    /* 입금 확인 관련 이벤트 처리 */

    // 입금 확인: 성공 확인됨
    @Override
    public void handle(DepositSuccessChecked event) {
        // 환전 종료 처리
        completeExchangeRequestService.succeedExchange(
                event.requestId()
        );
    }

    // 입금 확인: 실패 확인됨
    @Override
    public void handle(DepositFailureChecked event) {
        // 출금 취소
        cancelWithdrawalService.cancelWithdrawal(
                event.exchangeId(),
                event.direction()
        );
    }

    // 입금 확인: 결과 알 수 없음
    @Override
    public void handle(DepositCheckUnknown event) {
        // 입금 확인 지연
        scheduleCheckRequestPort.scheduleCheckRequest(
                event.exchangeId(),
                event.delay(),
                event.count(),
                event.direction(),
                TransactionType.DEPOSIT
        );
    }
    // 입금 확인: 확인 횟수 초과
    @Override
    public void handle(DepositAttemptExhausted event) {
        // 경고 메시지 발송
        sendWarningMessagePort.sendWarningMessage(
                event.requestId(),
                event.exchangeId()
        );
    }

    /* 출금 취소 이벤트 처리 */
    @Override
    public void handle(WithdrawalCancelSucceeded event) {
        // 환전 종료 처리
        completeExchangeRequestService.failExchange(
                event.requestId()
        );
    }
}
