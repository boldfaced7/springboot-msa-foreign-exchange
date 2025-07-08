package com.boldfaced7.fxexchange.exchange.adapter.out.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "web-client")
public record WebClientBaseUrlProperties(
        String fxAccountBaseUrl,
        String krwAccountBaseUrl
) {}
