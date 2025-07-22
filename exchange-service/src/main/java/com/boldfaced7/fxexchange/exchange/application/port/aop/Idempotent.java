package com.boldfaced7.fxexchange.exchange.application.port.aop;

import com.boldfaced7.fxexchange.exchange.application.exception.DuplicateRequestException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    String key();
    String prefix();

    Class<? extends RuntimeException> exceptionOnDuplicate() default DuplicateRequestException.class;

    long expiration() default 24L;
    TimeUnit timeUnit() default TimeUnit.HOURS;
} 