package com.boldfaced7.fxexchange.exchange.domain.vo.deposit;

public record Deposited(boolean value) {
    public static final Deposited DEPOSITED = new Deposited(true);
    public static final Deposited NOT_DEPOSITED = new Deposited(false);
}
