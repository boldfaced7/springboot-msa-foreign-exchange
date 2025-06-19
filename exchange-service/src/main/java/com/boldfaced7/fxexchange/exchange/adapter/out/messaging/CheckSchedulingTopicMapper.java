package com.boldfaced7.fxexchange.exchange.adapter.out.messaging;

import com.boldfaced7.fxexchange.exchange.domain.enums.TransactionCheckType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class CheckSchedulingTopicMapper {

    @Value("${kafka.topic.exchange.deposit-check-topic}")
    private String depositCheckTopic;

    @Value("${kafka.topic.exchange.withdrawal-check-topic}")
    private String withdrawalCheckTopic;

    private final Map<TransactionCheckType, String> topicMap = new EnumMap<>(TransactionCheckType.class);

    @PostConstruct
    public void init() {
        topicMap.put(TransactionCheckType.DEPOSIT_RESULT, depositCheckTopic);
        topicMap.put(TransactionCheckType.WITHDRAWAL_RESULT, withdrawalCheckTopic);
    }

    public String getTopic(TransactionCheckType transactionCheckType) {
        return topicMap.get(transactionCheckType);
    }
}
