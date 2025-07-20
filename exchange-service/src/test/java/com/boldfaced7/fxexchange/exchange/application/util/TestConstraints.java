package com.boldfaced7.fxexchange.exchange.application.util;

import com.boldfaced7.fxexchange.exchange.adapter.out.external.account.TransactionResponse;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckDepositWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckWithdrawalWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.CompleteWithdrawalCancelCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.ExchangeCurrencyCommand;
import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.model.Deposit;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.model.Withdrawal;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;
import com.boldfaced7.fxexchange.exchange.domain.vo.cancel.WithdrawalCancelId;
import com.boldfaced7.fxexchange.exchange.domain.vo.deposit.DepositId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.*;
import com.boldfaced7.fxexchange.exchange.domain.vo.withdrawal.WithdrawalId;

import java.time.LocalDateTime;

public class TestConstraints {
    public static final RequestId REQUEST_ID = new RequestId(1L);
    public static final ExchangeId EXCHANGE_ID = new ExchangeId("exchangeId");
    public static final UserId USER_ID = new UserId("userId");

    public static final Direction DIRECTION_BUY = Direction.BUY;
    public static final Direction DIRECTION_SELL = Direction.SELL;

    public static final BaseCurrency BASE_CURRENCY = new BaseCurrency(CurrencyCode.USD);
    public static final QuoteCurrency QUOTE_CURRENCY = new QuoteCurrency(CurrencyCode.KRW);

    public static final BaseAmount BASE_AMOUNT = new BaseAmount(1);
    public static final QuoteAmount QUOTE_AMOUNT = new QuoteAmount(1400);
    public static final ExchangeRate EXCHANGE_RATE = new ExchangeRate(1400.0);

    public static final WithdrawalId WITHDRAWAL_ID = new WithdrawalId("withdrawalId");
    public static final DepositId DEPOSIT_ID = new DepositId("depositId");
    public static final WithdrawalCancelId WITHDRAWAL_CANCEL_ID = new WithdrawalCancelId("withdrawalCancelId");

    public static final Exchanged EXCHANGE_FINISHED = new Exchanged(true);
    public static final Exchanged NOT_EXCHANGE_FINISHED = new Exchanged(false);

    public static final Count COUNT_ZERO = new Count(0);
    public static final Count COUNT_ONE = new Count(1);
    public static final Count COUNT_TWO = new Count(2);
    public static final Count COUNT_THREE = new Count(2);


    public static final ExchangeRequest EXCHANGE_REQUEST = ExchangeRequest.of(
            REQUEST_ID, EXCHANGE_ID, USER_ID,
            Direction.BUY, BASE_CURRENCY, QUOTE_CURRENCY,
            BASE_AMOUNT, QUOTE_AMOUNT, EXCHANGE_RATE,
            NOT_EXCHANGE_FINISHED, LocalDateTime.now(), LocalDateTime.now()
    );


    public static final ExchangeCurrencyCommand EXCHANGE_CURRENCY_COMMAND = new ExchangeCurrencyCommand(
            EXCHANGE_ID,
            USER_ID,
            BASE_CURRENCY,
            BASE_AMOUNT,
            QUOTE_AMOUNT,
            DIRECTION_BUY,
            EXCHANGE_RATE
    );

    public static final CompleteWithdrawalCancelCommand COMPLETE_WITHDRAWAL_CANCEL_COMMAND = new CompleteWithdrawalCancelCommand(
            WITHDRAWAL_CANCEL_ID,
            EXCHANGE_ID,
            DIRECTION_BUY
    );

    // TransactionResponse 미리 정의
    public static final TransactionResponse WITHDRAWAL_SUCCESS = new TransactionResponse(true, "SUCCESS", "withdrawalId");
    public static final TransactionResponse WITHDRAWAL_FAILED = new TransactionResponse(false, "FAILED", null);
    public static final TransactionResponse DEPOSIT_SUCCESS = new TransactionResponse(true, "SUCCESS", "depositId");
    public static final TransactionResponse DEPOSIT_FAILED = new TransactionResponse(false, "FAILED", null);

    public static CheckDepositWithDelayCommand checkDepositWithDelayCommand(int count) {
        return new CheckDepositWithDelayCommand(
                EXCHANGE_ID,
                new Count(count),
                DIRECTION_BUY
        );
    }
    public static CheckWithdrawalWithDelayCommand checkWithdrawalWithDelayCommand(int count) {
        return new CheckWithdrawalWithDelayCommand(
                EXCHANGE_ID,
                new Count(count),
                DIRECTION_BUY
        );
    }

    public static Deposit deposit(boolean request, boolean success) {

        return Deposit.create(
                DEPOSIT_ID,
                REQUEST_ID,
                EXCHANGE_ID,
                USER_ID,
                Direction.BUY,
                success,
                LocalDateTime.now()
        );
    }

    public static Withdrawal withdrawal(boolean request, boolean success) {

        return Withdrawal.create(
                WITHDRAWAL_ID,
                REQUEST_ID,
                EXCHANGE_ID,
                USER_ID,
                Direction.BUY,
                success,
                LocalDateTime.now()
        );
    }


}
