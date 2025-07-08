package com.boldfaced7.fxexchange.exchange.adapter.out.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.scheduler")
public record KafkaSchedulerProperties(
        String schedulerTopic,
        String scheduledTimeMillisHeader
) {}