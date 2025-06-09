package com.boldfaced7.fxexchange.scheduler.adapter.out.persistence;

import com.boldfaced7.fxexchange.common.PersistenceAdapter;
import com.boldfaced7.fxexchange.scheduler.application.port.out.DeleteScheduledMessagePort;
import com.boldfaced7.fxexchange.scheduler.application.port.out.LoadScheduledMessagePort;
import com.boldfaced7.fxexchange.scheduler.application.port.out.SaveScheduledMessagePort;
import com.boldfaced7.fxexchange.scheduler.domain.model.ScheduledMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

@PersistenceAdapter
@RequiredArgsConstructor
public class RedisScheduledMessageAdapter implements
        LoadScheduledMessagePort,
        SaveScheduledMessagePort,
        DeleteScheduledMessagePort
{

    private static final String SCHEDULED_Z_SET_KEY = "scheduled::messages";

    private final RedisTemplate<String, ScheduledMessage> redisTemplate;

    @Override
    public Set<ScheduledMessage> loadDueMessages(long currentTimeMillis) {
        return redisTemplate.opsForZSet().rangeByScore(SCHEDULED_Z_SET_KEY, 0, currentTimeMillis);
    }

    @Override
    public void saveScheduledMessage(ScheduledMessage message) {
        redisTemplate.opsForZSet().add(SCHEDULED_Z_SET_KEY, message, message.scheduledTimeMillis());
    }

    @Override
    public void deleteAll(Set<ScheduledMessage> messages) {
        redisTemplate.opsForZSet().remove(SCHEDULED_Z_SET_KEY, messages.toArray());
    }

}
