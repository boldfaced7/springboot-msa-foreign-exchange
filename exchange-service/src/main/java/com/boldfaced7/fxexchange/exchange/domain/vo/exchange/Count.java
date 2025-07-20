package com.boldfaced7.fxexchange.exchange.domain.vo.exchange;

import com.boldfaced7.fxexchange.exchange.domain.exception.InvalidValueException;

public record Count(int value) {

    public Count {
        if (value < 0) 
            throw new InvalidValueException("카운트는 0 이상이어야 합니다.");
    }

    public static Count zero() {
        return new Count(0);
    }

    public Count increase() {
        return new Count(this.value + 1);
    }

    public boolean isSmallerThan(Count other) {
        return value < other.value;
    }

}
