package com.boldfaced7.fxexchange.exchange.adapter.out.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisExchangeIdIndexRepository {
    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "exchange-id:";

    public void save(RedisExchangeRequest exchangeRequest) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + exchangeRequest.getExchangeId(),
                exchangeRequest.getExchangeRequestId(),
                Duration.ofHours(1)
        );
    }
    public Optional<String> find(String exchangeId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + exchangeId));
    }

    public void delete(String exchangeId) {
        redisTemplate.delete(KEY_PREFIX + exchangeId);
    }
}
