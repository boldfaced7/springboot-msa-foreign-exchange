package com.boldfaced7.fxexchange.exchange.application.saga;

import com.boldfaced7.fxexchange.common.SagaOrchestrator;
import com.boldfaced7.fxexchange.exchange.application.service.buy.compensate.CancelKrwWithdrawalService;
import com.boldfaced7.fxexchange.exchange.application.service.buy.deposit.CheckFxDepositService;
import com.boldfaced7.fxexchange.exchange.application.service.buy.deposit.DelayFxDepositCheckService;
import com.boldfaced7.fxexchange.exchange.application.service.buy.deposit.DepositFxService;
import com.boldfaced7.fxexchange.exchange.application.service.buy.withdrawal.CheckKrwWithdrawalService;
import com.boldfaced7.fxexchange.exchange.application.service.buy.withdrawal.DelayKrwWithdrawalCheckService;
import com.boldfaced7.fxexchange.exchange.application.service.buy.withdrawal.WithdrawKrwService;
import com.boldfaced7.fxexchange.exchange.application.service.exchange.TerminateExchangeRequestService;
import com.boldfaced7.fxexchange.exchange.domain.event.buy.*;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;

@SagaOrchestrator
@RequiredArgsConstructor
public class BuyForeignCurrencySagaOrchestratorImpl implements
        BuyForeignCurrencySagaOrchestrator {

    private final WithdrawKrwService withdrawKrwService;
    private final DepositFxService depositFxService;

    private final CheckKrwWithdrawalService checkKrwWithdrawalService;
    private final DelayKrwWithdrawalCheckService delayKrwWithdrawalCheckService;

    private final CheckFxDepositService checkFxDepositService;
    private final DelayFxDepositCheckService delayFxDepositCheckService;

    private final CancelKrwWithdrawalService cancelKrwWithdrawalService;
    private final TerminateExchangeRequestService terminateExchangeRequestService;

    @Override
    public ExchangeDetail startExchange(ExchangeRequest requested) {
        var withdrawn = withdrawKrwService.withdrawKrw(requested);
        var deposited = depositFxService.depositFx(withdrawn.exchangeRequest());
        return new ExchangeDetail(withdrawn, deposited);
    }

    /*
        환전(외화 구매) 관련 이벤트 처리
     */
    @Async
    @TransactionalEventListener
    public void handle(BuyingStarted event) {

    }

    @Async
    @TransactionalEventListener
    public void handle(BuyingFailed event) {
        terminateExchangeRequestService.terminateExchangeRequest(
                event.requestId()
        );
    }

    @Async
    @TransactionalEventListener
    public void handle(BuyingCompleted event) {
        terminateExchangeRequestService.terminateExchangeRequest(
                event.requestId()
        );
    }

    /*
        원화 출금 관련 이벤트 처리
     */
    @Async
    @TransactionalEventListener
    public void handle(CheckingKrwWithdrawalRequired event) {
        checkKrwWithdrawalService.checkKrwWithdrawal(
                event.requestId(),
                event.count()
        );
    }

    @Async
    @TransactionalEventListener
    public void handle(DelayingKrwWithdrawalCheckRequired event) {
        delayKrwWithdrawalCheckService.delayKrwWithdrawalCheck(
                event.requestId(),
                event.count()
        );
    }

    @Async
    @TransactionalEventListener
    public void handle(CancelingKrwWithdrawalRequired event) {
        cancelKrwWithdrawalService.cancelKrwWithdrawal(
                event.exchangeId()
        );
    }

    @Async
    @TransactionalEventListener
    public void handle(KrwWithdrawalCompleted event) {

    }

    /*
        외화 입금 관련 이벤트 처리
     */
    @Async
    @TransactionalEventListener
    public void handle(CheckingFxDepositRequired event) {
        checkFxDepositService.checkFxDeposit(
                event.requestId(),
                event.count()
        );
    }

    @Async
    @TransactionalEventListener
    public void handle(DelayingFxDepositCheckRequired event) {
        delayFxDepositCheckService.delayFxDepositCheck(
                event.requestId(),
                event.count()
        );
    }
}
