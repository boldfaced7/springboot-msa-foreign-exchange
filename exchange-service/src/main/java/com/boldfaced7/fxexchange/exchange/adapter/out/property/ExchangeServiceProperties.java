package com.boldfaced7.fxexchange.exchange.adapter.out.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "exchange")
public record ExchangeServiceProperties(
        int maxCountCheck,
        int delaySecond
) {
}
