package com.boldfaced7.fxexchange.scheduler.adapter.in.messaging;

import com.boldfaced7.fxexchange.scheduler.adapter.in.AbstractIntegrationTest;
import com.boldfaced7.fxexchange.scheduler.domain.model.ScheduledMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class MessageSchedulerTest extends AbstractIntegrationTest {

    private static final String SCHEDULED_Z_SET_KEY = "scheduled::messages";
    private static final String SCHEDULED_TIME_MILLIS = "scheduled-time-millis";

    @Value("${kafka.topic}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private RedisTemplate<String, ScheduledMessage> redisTemplate;

    @Test
    void shouldScheduleMessage() {
        // given
        String payload = "test-payload";
        long scheduledTimeMillis = System.currentTimeMillis() + 100000; // 100초 후

        // when
        kafkaTemplate.send(MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.RECEIVED_TOPIC, topic)
                .setHeader(SCHEDULED_TIME_MILLIS, scheduledTimeMillis)
                .build());

        // then: 메시지가 Redis ZSet에 저장될 때까지 최대 2초간 기다린다.
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            Set<ScheduledMessage> scheduledMessages = redisTemplate.opsForZSet()
                    .rangeByScore(SCHEDULED_Z_SET_KEY, scheduledTimeMillis, scheduledTimeMillis);

            assertThat(scheduledMessages).hasSize(1);
            scheduledMessages.forEach(s -> {
                assertThat(s.originalTopic()).isEqualTo(topic);
                assertThat(s.payload()).isEqualTo(payload);
                assertThat(s.scheduledTimeMillis()).isEqualTo(scheduledTimeMillis);
            });
        });
    }
} 