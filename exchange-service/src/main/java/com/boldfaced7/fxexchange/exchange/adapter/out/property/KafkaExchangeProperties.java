package com.boldfaced7.fxexchange.exchange.adapter.out.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.exchange")
public record KafkaExchangeProperties(
        String dltTopic,
        String depositCheckTopic,
        String withdrawalCheckTopic
) {}