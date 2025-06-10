package com.boldfaced7.fxexchange.scheduler.adapter.in.scheduling;

import com.boldfaced7.fxexchange.scheduler.adapter.in.AbstractIntegrationTest;
import com.boldfaced7.fxexchange.scheduler.domain.model.ScheduledMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class DueMessageSenderTest extends AbstractIntegrationTest {

    private static final String SCHEDULED_Z_SET_KEY = "scheduled::messages";

    @Value("${kafka.topic}")
    private String topic;

    @Autowired
    private RedisTemplate<String, ScheduledMessage> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.delete(SCHEDULED_Z_SET_KEY);
    }

    @Test
    void shouldSendDueMessages() {
        // given
        String payload = "test-payload";
        long scheduledTimeMillis = System.currentTimeMillis() - 1000; // 1초 전에 예약된 메시지
        var dueMessage = new ScheduledMessage(topic, scheduledTimeMillis, payload);

        redisTemplate.opsForZSet().add(SCHEDULED_Z_SET_KEY, dueMessage, scheduledTimeMillis);

        // when & then: 스케줄러가 실행되어 Redis의 메시지가 삭제될 때까지 최대 2초간 기다린다.
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            Long size = redisTemplate.opsForZSet().size(SCHEDULED_Z_SET_KEY);
            assertThat(size).isZero();
        });
    }
} 