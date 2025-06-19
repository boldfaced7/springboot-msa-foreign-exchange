package com.boldfaced7.fxexchange.exchange.application.saga;

import com.boldfaced7.fxexchange.exchange.domain.event.deposit.*;
import com.boldfaced7.fxexchange.exchange.domain.event.request.ExchangeCurrencyFailed;
import com.boldfaced7.fxexchange.exchange.domain.event.request.ExchangeCurrencyStarted;
import com.boldfaced7.fxexchange.exchange.domain.event.request.ExchangeCurrencySucceeded;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.*;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeDetail;

public interface ExchangeCurrencySagaOrchestrator {
    ExchangeDetail startExchange(ExchangeRequest requested);

    void handle(ExchangeCurrencyStarted event);
    void handle(ExchangeCurrencySucceeded event);
    void handle(ExchangeCurrencyFailed event);

    void handle(WithdrawalSucceeded event);
    void handle(WithdrawalFailed event);
    void handle(WithdrawalResultUnknown event);
    void handle(WithdrawalSuccessChecked event);
    void handle(WithdrawalFailureChecked event);
    void handle(WithdrawalCancelled event);
    void handle(WithdrawalCheckUnknown event);

    void handle(DepositSucceeded event);
    void handle(DepositFailed event);
    void handle(DepositResultUnknown event);
    void handle(DepositSuccessChecked event);
    void handle(DepositFailureChecked event);
    void handle(DepositCheckUnknown event);
}
