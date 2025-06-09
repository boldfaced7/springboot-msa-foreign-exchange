package com.boldfaced7.fxexchange.exchange.adapter.out.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageSerializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String serializeMessage(Object message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error("메시지 직렬화 실패 - message: {}, error: {}", message, e.getMessage(), e);
            throw new RuntimeException("메시지 직렬화 실패", e);
        }
    }
}
