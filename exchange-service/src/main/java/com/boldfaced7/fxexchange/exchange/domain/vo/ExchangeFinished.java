package com.boldfaced7.fxexchange.exchange.domain.vo;

public record ExchangeFinished(boolean value) {
    public static final ExchangeFinished FINISHED = new ExchangeFinished(true);
    public static final ExchangeFinished NOT_FINISHED = new ExchangeFinished(false);
}
