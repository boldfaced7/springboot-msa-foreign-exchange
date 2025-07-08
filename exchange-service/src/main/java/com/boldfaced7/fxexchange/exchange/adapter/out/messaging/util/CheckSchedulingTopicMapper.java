package com.boldfaced7.fxexchange.exchange.adapter.out.messaging.util;

import com.boldfaced7.fxexchange.exchange.adapter.out.property.KafkaExchangeProperties;
import com.boldfaced7.fxexchange.exchange.domain.enums.TransactionType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CheckSchedulingTopicMapper {

    private final KafkaExchangeProperties checkSchedulingTopic;

    private final Map<TransactionType, String> topicMap = new EnumMap<>(TransactionType.class);

    @PostConstruct
    public void init() {
        topicMap.put(TransactionType.DEPOSIT, checkSchedulingTopic.depositCheckTopic());
        topicMap.put(TransactionType.WITHDRAWAL, checkSchedulingTopic.withdrawalCheckTopic());
    }

    public String getTopic(TransactionType transactionType) {
        return topicMap.get(transactionType);
    }
}
