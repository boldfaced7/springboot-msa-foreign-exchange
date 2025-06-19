package com.boldfaced7.fxexchange.exchange.adapter.out.messaging;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class WithdrawalCancelTopicMapper {

    @Value("${kafka.topic.fx-account.withdrawal-cancel-request-topic}")
    private String fxWithdrawalCancelRequestTopic;

    @Value("${kafka.topic.krw-account.withdrawal-cancel-request-topic}")
    private String krwWithdrawalCancelRequestTopic;

    @Value("${kafka.topic.fx-account.withdrawal-cancel-response-topic}")
    private String fxWithdrawalCancelResponseTopic;

    @Value("${kafka.topic.krw-account.withdrawal-cancel-response-topic}")
    private String krwWithdrawalCancelResponseTopic;

    private final Map<Direction, String> requestTopicMap = new EnumMap<>(Direction.class);
    private final Map<Direction, String> responseTopicMap = new EnumMap<>(Direction.class);

    @PostConstruct
    public void init() {
        requestTopicMap.put(Direction.BUY, krwWithdrawalCancelRequestTopic);
        requestTopicMap.put(Direction.SELL, fxWithdrawalCancelRequestTopic);

        responseTopicMap.put(Direction.BUY, krwWithdrawalCancelResponseTopic);
        responseTopicMap.put(Direction.SELL, fxWithdrawalCancelResponseTopic);
    }

    public String getRequestTopic(Direction direction) {
        return requestTopicMap.get(direction);
    }

    public String getResponseTopic(Direction direction) {
        return responseTopicMap.get(direction);
    }

}