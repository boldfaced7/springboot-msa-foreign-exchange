package com.boldfaced7.fxexchange.scheduler.adapter.in.messaging;

import com.boldfaced7.fxexchange.scheduler.application.port.in.ScheduleMessageCommand;
import com.boldfaced7.fxexchange.scheduler.application.port.in.ScheduleMessageUseCase;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageScheduler {

    private static final String SCHEDULED_TIME_MILLIS = "scheduled-time-millis";

    private final ScheduleMessageUseCase scheduleMessageUseCase;

    @KafkaListener(
            topics = "${kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void scheduleMessages(
            List<ConsumerRecord<String, String>> records,
            Acknowledgment ack
    ) {
        records.forEach(record -> {
            String scheduledTimeMillis = new String(
                    record.headers().lastHeader(SCHEDULED_TIME_MILLIS).value()
            );
            scheduleMessage(
                    record.topic(),
                    scheduledTimeMillis,
                    record.value()
            );
        });
        ack.acknowledge();
    }

    private void scheduleMessage(
            String originalTopic,
            String scheduledTimeMillis,
            String payload
    ) {
        var command = new ScheduleMessageCommand(
                originalTopic,
                Long.parseLong(scheduledTimeMillis),
                payload
        );
        scheduleMessageUseCase.scheduleMessage(command);
    }

}
