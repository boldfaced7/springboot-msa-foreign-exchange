package com.boldfaced7.fxexchange.exchange.adapter.out.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.fx-account")
public record KafkaFxAccountProperties(
        String withdrawalCancelRequestTopic,
        String withdrawalCancelResponseTopic
) {}