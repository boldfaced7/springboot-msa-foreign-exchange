package com.boldfaced7.fxexchange.exchange.domain.enums;

import com.boldfaced7.fxexchange.exchange.domain.event.deposit.DepositCheckUnknown;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.WithdrawalCheckUnknown;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum TransactionCheckType {
    DEPOSIT_RESULT(DepositCheckUnknown.class),
    WITHDRAWAL_RESULT(WithdrawalCheckUnknown.class);
    
    private final Class<?> eventClass;
    
    TransactionCheckType(Class<?> eventClass) {
        this.eventClass = eventClass;
    }
    
    public static TransactionCheckType fromEventClass(Class<?> eventClass) {
        return Arrays.stream(values())
                .filter(type -> type.getEventClass().equals(eventClass))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown event class: " + eventClass.getName()));
    }
}
