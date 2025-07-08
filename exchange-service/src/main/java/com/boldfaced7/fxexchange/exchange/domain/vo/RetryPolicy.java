package com.boldfaced7.fxexchange.exchange.domain.vo;

import java.time.Duration;

public record RetryPolicy(
        Count criteria,
        Duration baseDelay
) {
    public RetryPolicy(int maxCountCheck, int delaySecond) {
        this(new Count(maxCountCheck), Duration.ofSeconds(delaySecond));
    }

    public Duration calculateDelay(Count count) {
        return baseDelay.multipliedBy(count.value() + 1);
    }

}