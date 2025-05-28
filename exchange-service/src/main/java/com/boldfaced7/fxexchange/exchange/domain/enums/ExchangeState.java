package com.boldfaced7.fxexchange.exchange.domain.enums;

import com.boldfaced7.fxexchange.exchange.domain.event.buy.BuyingCompleted;
import com.boldfaced7.fxexchange.exchange.domain.event.buy.BuyingFailed;
import com.boldfaced7.fxexchange.exchange.domain.event.buy.BuyingStarted;
import com.boldfaced7.fxexchange.exchange.domain.event.buy.CheckingFxDepositRequired;
import com.boldfaced7.fxexchange.exchange.domain.event.buy.CheckingKrwWithdrawalRequired;
import com.boldfaced7.fxexchange.exchange.domain.event.buy.CancelingKrwWithdrawalRequired;
import com.boldfaced7.fxexchange.exchange.domain.event.buy.KrwWithdrawalCompleted;
import com.boldfaced7.fxexchange.exchange.domain.event.sell.CancelingFxWithdrawalRequired;
import com.boldfaced7.fxexchange.exchange.domain.event.sell.CheckingFxWithdrawalRequired;
import com.boldfaced7.fxexchange.exchange.domain.event.sell.CheckingKrwDepositRequired;
import com.boldfaced7.fxexchange.exchange.domain.event.sell.FxWithdrawalCompleted;
import com.boldfaced7.fxexchange.exchange.domain.event.sell.SellingCompleted;
import com.boldfaced7.fxexchange.exchange.domain.event.sell.SellingFailed;
import com.boldfaced7.fxexchange.exchange.domain.event.sell.SellingStarted;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ExchangeState {
    // Buying
    BUYING_STARTED(BuyingStarted.class),
    BUYING_COMPLETED(BuyingCompleted.class),
    BUYING_FAILED(BuyingFailed.class),

    KRW_WITHDRAWAL_COMPLETED(KrwWithdrawalCompleted.class),
    CHECKING_KRW_WITHDRAWAL_REQUIRED(CheckingKrwWithdrawalRequired.class),
    CANCELING_KRW_WITHDRAWAL_REQUIRED(CancelingKrwWithdrawalRequired.class),

    CHECKING_FX_DEPOSIT_REQUIRED(CheckingFxDepositRequired.class),

    // Selling
    SELLING_STARTED(SellingStarted.class),
    SELLING_COMPLETED(SellingCompleted.class),
    SELLING_FAILED(SellingFailed.class),

    FX_WITHDRAWAL_COMPLETED(FxWithdrawalCompleted.class),
    CHECKING_FX_WITHDRAWAL_REQUIRED(CheckingFxWithdrawalRequired.class),
    CANCELING_FX_WITHDRAWAL_REQUIRED(CancelingFxWithdrawalRequired.class),

    CHECKING_KRW_DEPOSIT_REQUIRED(CheckingKrwDepositRequired.class);

    private final Class<?> eventClass;

    private static final Map<Class<?>, ExchangeState> BY_EVENT_CLASS_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(
                    ExchangeState::getEventClass,
                    Function.identity()
            ));

    ExchangeState(Class<?> eventClass) {
        this.eventClass = eventClass;
    }

    public Class<?> getEventClass() {
        return eventClass;
    }

    public static ExchangeState fromEventClass(Class<?> eventClass) {
        ExchangeState state = BY_EVENT_CLASS_MAP.get(eventClass);
        if (state == null) {
            throw new IllegalArgumentException("Invalid exchange state: " + eventClass);
        }
        return state;
    }
}
