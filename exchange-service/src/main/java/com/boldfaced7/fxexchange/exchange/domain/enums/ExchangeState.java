package com.boldfaced7.fxexchange.exchange.domain.enums;

import com.boldfaced7.fxexchange.exchange.domain.event.deposit.DepositFailed;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.DepositFailureChecked;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.DepositResultUnknown;
import com.boldfaced7.fxexchange.exchange.domain.event.request.ExchangeCurrencyFailed;
import com.boldfaced7.fxexchange.exchange.domain.event.request.ExchangeCurrencyStarted;
import com.boldfaced7.fxexchange.exchange.domain.event.request.ExchangeCurrencySucceeded;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalResultUnknown;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalSucceeded;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalSuccessChecked;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum ExchangeState {
    EXCHANGE_CURRENCY_STARTED(Set.of(
            ExchangeCurrencyStarted.class
    )),
    EXCHANGE_CURRENCY_SUCCEEDED(Set.of(
            ExchangeCurrencySucceeded.class
    )),
    EXCHANGE_CURRENCY_FAILED(Set.of(
            ExchangeCurrencyFailed.class
    )),

    WITHDRAWAL_SUCCEEDED(Set.of(
            WithdrawalSucceeded.class
    )),
    CHECKING_WITHDRAWAL_REQUIRED(Set.of(
            WithdrawalResultUnknown.class
    )),
    CANCELING_WITHDRAWAL_REQUIRED(Set.of(
            WithdrawalSuccessChecked.class,
            DepositFailureChecked.class,
            DepositFailed.class
    )),

    CHECKING_DEPOSIT_REQUIRED(Set.of(
            DepositResultUnknown.class
    ));

    private final Set<Class<?>> eventClasses;

    private static final Map<Class<?>, ExchangeState> BY_EVENT_CLASS_MAP = Arrays.stream(values())
            .flatMap(state -> state.getEventClasses().stream()
                    .map(eventClass -> Map.entry(eventClass, state)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    ExchangeState(Set<Class<?>> eventClasses) {
        this.eventClasses = eventClasses;
    }

    public static ExchangeState fromEventClass(Class<?> eventClass) {
        return Optional.ofNullable(BY_EVENT_CLASS_MAP.get(eventClass))
                .orElseThrow(() -> new RuntimeException("Unknown event class " + eventClass.getName()));
    }
}
