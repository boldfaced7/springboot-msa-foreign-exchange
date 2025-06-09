package com.boldfaced7.fxexchange.scheduler.adapter.in.messaging;

import com.boldfaced7.fxexchange.scheduler.application.port.in.ScheduleMessageCommand;
import com.boldfaced7.fxexchange.scheduler.application.port.in.ScheduleMessageUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageScheduler {

    private static final String SCHEDULED_TIME_MILLIS = "scheduled-time-millis";

    private final ScheduleMessageUseCase scheduleMessageUseCase;

    @KafkaListener(
            topics = "${kafka.topic.check}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void scheduleMessage(
            @Header(KafkaHeaders.RECEIVED_TOPIC) String originalTopic,
            @Header(SCHEDULED_TIME_MILLIS) long scheduledTimeMillis,
            @Payload String payload,
            Acknowledgment ack
    ) {
        var command = new ScheduleMessageCommand(originalTopic, scheduledTimeMillis, payload);
        scheduleMessageUseCase.scheduleMessage(command);
        ack.acknowledge();
    }
}
