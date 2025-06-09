package com.boldfaced7.fxexchange.scheduler.application.port.in;

public record ScheduleMessageCommand(
        String originalTopic,
        long scheduledTimeMillis,
        String payload
) {
}
