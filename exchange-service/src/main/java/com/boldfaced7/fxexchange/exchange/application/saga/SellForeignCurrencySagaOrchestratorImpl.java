package com.boldfaced7.fxexchange.exchange.application.saga;

import com.boldfaced7.fxexchange.common.SagaOrchestrator;
import com.boldfaced7.fxexchange.exchange.application.service.exchange.TerminateExchangeRequestService;
import com.boldfaced7.fxexchange.exchange.application.service.sell.compensate.CancelFxWithdrawalService;
import com.boldfaced7.fxexchange.exchange.application.service.sell.deposit.CheckKrwDepositService;
import com.boldfaced7.fxexchange.exchange.application.service.sell.deposit.DelayKrwDepositCheckService;
import com.boldfaced7.fxexchange.exchange.application.service.sell.deposit.DepositKrwService;
import com.boldfaced7.fxexchange.exchange.application.service.sell.withdrawal.CheckFxWithdrawalService;
import com.boldfaced7.fxexchange.exchange.application.service.sell.withdrawal.DelayFxWithdrawalCheckService;
import com.boldfaced7.fxexchange.exchange.application.service.sell.withdrawal.WithdrawFxService;
import com.boldfaced7.fxexchange.exchange.domain.event.sell.*;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;

@SagaOrchestrator
@RequiredArgsConstructor
public class SellForeignCurrencySagaOrchestratorImpl implements
        SellForeignCurrencySagaOrchestrator {

    private final WithdrawFxService withdrawFxService;
    private final DepositKrwService depositKrwService;

    private final CheckFxWithdrawalService checkFxWithdrawalService;
    private final DelayFxWithdrawalCheckService delayFxWithdrawalCheckService;

    private final CheckKrwDepositService checkKrwDepositService;
    private final DelayKrwDepositCheckService delayKrwDepositCheckService;

    private final CancelFxWithdrawalService cancelFxWithdrawalService;
    private final TerminateExchangeRequestService terminateExchangeRequestService;

    @Override
    public ExchangeDetail startExchange(ExchangeRequest requested) {
        var withdrawn = withdrawFxService.withdrawFx(requested);
        var deposited = depositKrwService.depositKrw(withdrawn.exchangeRequest());
        return new ExchangeDetail(withdrawn, deposited);
    }

    /*
        환전(외화 판매) 관련 이벤트 처리
     */
    @Async
    @TransactionalEventListener
    public void handle(SellingStarted event) {

    }

    @Async
    @TransactionalEventListener
    public void handle(SellingFailed event) {
        terminateExchangeRequestService.terminateExchangeRequest(
                event.requestId()
        );
    }

    @Async
    @TransactionalEventListener
    public void handle(SellingCompleted event) {
        terminateExchangeRequestService.terminateExchangeRequest(
                event.requestId()
        );
    }

    /*
        외화 출금 관련 이벤트 처리
     */
    @Async
    @TransactionalEventListener
    public void handle(CheckingFxWithdrawalRequired event) {
        checkFxWithdrawalService.checkFxWithdrawal(
                event.requestId(),
                event.count()
        );
    }

    @Async
    @TransactionalEventListener
    public void handle(DelayingFxWithdrawalCheckRequired event) {
        delayFxWithdrawalCheckService.delayFxWithdrawalCheck(
                event.requestId(),
                event.count()
        );
    }

    @Async
    @TransactionalEventListener
    public void handle(CancelingFxWithdrawalRequired event) {
        cancelFxWithdrawalService.cancelFxWithdrawal(
                event.exchangeId()
        );
    }

    @Async
    @TransactionalEventListener
    public void handle(FxWithdrawalCompleted event) {

    }

    /*
        원화 입금 관련 이벤트 처리
     */
    @Async
    @TransactionalEventListener
    public void handle(CheckingKrwDepositRequired event) {
        checkKrwDepositService.checkKrwDeposit(
                event.requestId(),
                event.count()
        );
    }

    @Async
    @TransactionalEventListener
    public void handle(DelayingKrwDepositCheckRequired event) {
        delayKrwDepositCheckService.delayKrwDepositCheck(
                event.requestId(),
                event.count()
        );
    }
}
