package com.boldfaced7.fxexchange.exchange.adapter.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerConfig {

    @Bean
    @Qualifier("fxCircuitBreaker")
    public CircuitBreaker fxCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("fxCircuitBreaker");
    }

    @Bean
    @Qualifier("krwCircuitBreaker")
    public CircuitBreaker krwCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("krwCircuitBreaker");
    }
}