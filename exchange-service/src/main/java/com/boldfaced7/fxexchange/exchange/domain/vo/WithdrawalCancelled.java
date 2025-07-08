package com.boldfaced7.fxexchange.exchange.domain.vo;

public record WithdrawalCancelled(boolean value) {
    public static final WithdrawalCancelled CANCELLED = new WithdrawalCancelled(true);
    public static final WithdrawalCancelled NOT_CANCELLED = new WithdrawalCancelled(false);
}
