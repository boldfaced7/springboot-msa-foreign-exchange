package com.boldfaced7.fxexchange.exchange.domain.vo.withdrawal;

public record Withdrawn(boolean value) {
    public static final Withdrawn WITHDRAWN = new Withdrawn(true);
    public static final Withdrawn NOT_WITHDRAWN = new Withdrawn(false);
}
