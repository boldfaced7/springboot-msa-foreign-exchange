package com.boldfaced7.fxexchange.exchange.adapter.out.messaging;

import com.boldfaced7.fxexchange.common.MessagingAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.out.SendWithdrawalCheckRequestPort;
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
public class WithdrawalCheckKafkaPublisher implements
        SendWithdrawalCheckRequestPort
{
    private static final String TOPIC_WITHDRAWAL_CHECK = "exchange-topic.withdrawal.check";
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MessageSerializer messageSerializer;

    @Override
    public void sendWithdrawalCheckRequest(ExchangeId exchangeId, Duration delay, Count count, Direction direction) {
        var request = new WithdrawalCheckWithDelayRequest(exchangeId, delay, count, direction);
        var message = messageSerializer.serializeMessage(request);
        
        kafkaTemplate.send(TOPIC_WITHDRAWAL_CHECK, exchangeId.value(), message);
    }

    private record WithdrawalCheckWithDelayRequest(
            String exchangeId,
            long delaySeconds,
            int count,
            Direction direction
    ) {
        public WithdrawalCheckWithDelayRequest(
                ExchangeId exchangeId,
                Duration delay,
                Count count,
                Direction direction
        ) {
            this(exchangeId.value(), delay.getSeconds(), count.value(), direction);
        }
    }
}
