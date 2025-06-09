package com.boldfaced7.fxexchange.scheduler.domain.model;

public record ScheduledMessage(
        String originalTopic,
        long scheduledTimeMillis,
        String payload
) {}
