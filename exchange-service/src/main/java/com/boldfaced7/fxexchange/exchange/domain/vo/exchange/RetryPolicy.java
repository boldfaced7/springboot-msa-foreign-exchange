package com.boldfaced7.fxexchange.exchange.domain.vo.exchange;

import com.boldfaced7.fxexchange.exchange.domain.exception.InvalidValueException;

import java.time.Duration;

public record RetryPolicy(
        Count criteria,
        Duration baseDelay
) {
    public RetryPolicy {
        if (criteria == null) 
            throw new InvalidValueException("재시도 기준은 null일 수 없습니다.");
        if (baseDelay == null || baseDelay.isNegative()) 
            throw new InvalidValueException("기본 지연 시간은 null이거나 음수일 수 없습니다.");
    }

    public RetryPolicy(int maxCountCheck, int delaySecond) {
        this(new Count(maxCountCheck), Duration.ofSeconds(delaySecond));
    }

    public Duration calculateDelay(Count count) {
        return baseDelay.multipliedBy(count.value() + 1);
    }

}