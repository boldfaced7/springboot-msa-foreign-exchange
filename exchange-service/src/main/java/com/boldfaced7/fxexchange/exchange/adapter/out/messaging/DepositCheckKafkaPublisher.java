package com.boldfaced7.fxexchange.exchange.adapter.out.messaging;

import com.boldfaced7.fxexchange.common.MessagingAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.out.SendDepositCheckRequestPort;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;

@Slf4j
@Profile("!test")
@MessagingAdapter
@RequiredArgsConstructor
public class DepositCheckKafkaPublisher implements
        SendDepositCheckRequestPort
{
    private static final String TOPIC_DEPOSIT_CHECK = "exchange-topic.deposit.check";
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MessageSerializer messageSerializer;

    @Override
    public void sendDepositCheckRequest(ExchangeId exchangeId, Duration delay, Count count, Direction direction) {
        var request = new DepositCheckWithDelayRequest(exchangeId, delay, count, direction);
        var message = messageSerializer.serializeMessage(request);
        
        kafkaTemplate.send(TOPIC_DEPOSIT_CHECK, exchangeId.value(), message);
    }

    private record DepositCheckWithDelayRequest(
            String exchangeId,
            long delaySeconds,
            int count,
            Direction direction
    ) {
        public DepositCheckWithDelayRequest(
                ExchangeId exchangeId,
                Duration delay,
                Count count,
                Direction direction
        ) {
            this(exchangeId.value(), delay.getSeconds(), count.value(), direction);
        }
    }
}
