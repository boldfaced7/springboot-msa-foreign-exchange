package com.boldfaced7.fxexchange.exchange.adapter.out.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.krw-account")
public record KafkaKrwAccountProperties(
        String withdrawalCancelRequestTopic,
        String withdrawalCancelResponseTopic
) {}