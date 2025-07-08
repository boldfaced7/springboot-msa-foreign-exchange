package com.boldfaced7.fxexchange.exchange.application.service.saga;

import com.boldfaced7.fxexchange.exchange.domain.event.cancel.WithdrawalCancelSucceeded;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.*;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.*;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeDetail;

public interface ExchangeCurrencySagaOrchestrator {
    ExchangeDetail startExchange(ExchangeRequest exchange);

    // 출금 관련 이벤트 처리
    void handle(WithdrawalFailed event);
    void handle(WithdrawalUnknown event);

    void handle(WithdrawalSuccessChecked event);
    void handle(WithdrawalFailureChecked event);
    void handle(WithdrawalCheckUnknown event);
    void handle(WithdrawalAttemptExhausted event);

    // 입금 관련 이벤트 처리
    void handle(DepositFailed event);
    void handle(DepositUnknown event);

    void handle(DepositSuccessChecked event);
    void handle(DepositFailureChecked event);
    void handle(DepositCheckUnknown event);
    void handle(DepositAttemptExhausted event);

    // 출금 취소 이벤트 처리
    void handle(WithdrawalCancelSucceeded event);
}
