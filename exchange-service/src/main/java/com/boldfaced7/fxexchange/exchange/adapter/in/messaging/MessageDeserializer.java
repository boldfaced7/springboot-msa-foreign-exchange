package com.boldfaced7.fxexchange.exchange.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageDeserializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> T deserializeMessage(String message, Class<T> valueType) {
        try {
            return objectMapper.readValue(message, valueType);
        } catch (Exception e) {
            log.error("메시지 역직렬화 실패 - message: {}, error: {}", message, e.getMessage(), e);
            throw new RuntimeException("메시지 역직렬화 실패", e);
        }
    }
}
