package com.boldfaced7.fxexchange.exchange.application.port.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 락을 걸 대상 객체를 가리키는 SpEL.
     * 이 객체는 Identifiable 인터페이스를 구현해야 합니다.
     * 예: "#command.userId"
     */
    String key();

    /**
     * 락 키에 붙일 고정된 접두사(prefix).
     * 예: "balance:lock:"
     */
    String prefix();

    long waitTime() default 5L;
    long leaseTime() default 3L;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}