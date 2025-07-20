package com.boldfaced7.fxexchange.exchange.domain.vo.exchange;

public record Exchanged(boolean value) {
    public static final Exchanged FINISHED = new Exchanged(true);
    public static final Exchanged NOT_FINISHED = new Exchanged(false);
}
