package com.boldfaced7.fxexchange.exchange.adapter.out.messaging.util;

import com.boldfaced7.fxexchange.exchange.adapter.out.property.KafkaFxAccountProperties;
import com.boldfaced7.fxexchange.exchange.adapter.out.property.KafkaKrwAccountProperties;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WithdrawalCancelTopicMapper {

    private final KafkaFxAccountProperties fxAccount;
    private final KafkaKrwAccountProperties krwAccount;

    private final Map<Direction, String> requestTopicMap = new EnumMap<>(Direction.class);
    private final Map<Direction, String> responseTopicMap = new EnumMap<>(Direction.class);

    @PostConstruct
    public void init() {
        requestTopicMap.put(Direction.BUY, krwAccount.withdrawalCancelRequestTopic());
        requestTopicMap.put(Direction.SELL, fxAccount.withdrawalCancelRequestTopic());

        responseTopicMap.put(Direction.BUY, krwAccount.withdrawalCancelResponseTopic());
        responseTopicMap.put(Direction.SELL, fxAccount.withdrawalCancelResponseTopic());
    }

    public String getRequestTopic(Direction direction) {
        return requestTopicMap.get(direction);
    }

    public String getResponseTopic(Direction direction) {
        return responseTopicMap.get(direction);
    }
}