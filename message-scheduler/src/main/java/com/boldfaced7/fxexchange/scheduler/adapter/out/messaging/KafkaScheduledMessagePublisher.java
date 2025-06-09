package com.boldfaced7.fxexchange.scheduler.adapter.out.messaging;

import com.boldfaced7.fxexchange.common.MessagingAdapter;
import com.boldfaced7.fxexchange.scheduler.application.port.out.SendScheduledMessagePort;
import com.boldfaced7.fxexchange.scheduler.domain.model.ScheduledMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Set;

@MessagingAdapter
@RequiredArgsConstructor
public class KafkaScheduledMessagePublisher implements SendScheduledMessagePort {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void sendScheduledMessages(Set<ScheduledMessage> messages) {
        messages.forEach(message ->
                kafkaTemplate.send(message.originalTopic(), message.payload())
        );
    }
}
