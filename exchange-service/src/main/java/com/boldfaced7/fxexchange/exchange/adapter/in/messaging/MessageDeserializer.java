package com.boldfaced7.fxexchange.exchange.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageDeserializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static <T> T deserializeMessage(String message, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(message, valueType);
        } catch (Exception e) {
            log.error("메시지 역직렬화 실패 - message: {}, error: {}", message, e.getMessage(), e);
            throw new RuntimeException("메시지 역직렬화 실패", e);
        }
    }
}
