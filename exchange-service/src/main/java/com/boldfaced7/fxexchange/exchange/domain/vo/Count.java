package com.boldfaced7.fxexchange.exchange.domain.vo;

public record Count(int value) {

    public static Count zero() {
        return new Count(0);
    }

    public Count increase() {
        return new Count(this.value + 1);
    }

    public boolean isSmallerThan(int other) {
        return value < other;
    }
}
