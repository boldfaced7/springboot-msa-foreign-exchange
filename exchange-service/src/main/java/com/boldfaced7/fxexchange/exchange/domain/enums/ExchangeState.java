package com.boldfaced7.fxexchange.exchange.domain.enums;

import com.boldfaced7.fxexchange.exchange.domain.event.deposit.DepositFailed;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.DepositFailureChecked;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.DepositUnknown;
import com.boldfaced7.fxexchange.exchange.domain.event.exchange.ExchangeCurrencyFailed;
import com.boldfaced7.fxexchange.exchange.domain.event.exchange.ExchangeCurrencyStarted;
import com.boldfaced7.fxexchange.exchange.domain.event.exchange.ExchangeCurrencySucceeded;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalSucceeded;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalSuccessChecked;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalUnknown;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum ExchangeState {
    EXCHANGE_CURRENCY_STARTED,
    EXCHANGE_CURRENCY_SUCCEEDED,
    EXCHANGE_CURRENCY_FAILED,
    WITHDRAWAL_SUCCEEDED,
    CHECKING_WITHDRAWAL_REQUIRED,
    CHECKING_DEPOSIT_REQUIRED,
    CANCELING_WITHDRAWAL_REQUIRED;
}
