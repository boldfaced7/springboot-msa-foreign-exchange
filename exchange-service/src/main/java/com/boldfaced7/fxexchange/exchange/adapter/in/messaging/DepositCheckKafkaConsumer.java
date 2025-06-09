package com.boldfaced7.fxexchange.exchange.adapter.in.messaging;

import com.boldfaced7.fxexchange.common.MessagingAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckDepositWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckDepositWithDelayUseCase;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@MessagingAdapter
@Component
@RequiredArgsConstructor
public class DepositCheckKafkaConsumer {

    private final CheckDepositWithDelayUseCase checkDepositWithDelayUseCase;
    private final MessageDeserializer messageDeserializer;

    @KafkaListener(
            topics = "${kafka.topic.deposit-check}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message, Acknowledgment ack) {
        var checkMessage = messageDeserializer.deserializeMessage(message, DepositCheckMessage.class);
        var command = toCommand(checkMessage);
        checkDepositWithDelayUseCase.checkDepositWithDelay(command);
        ack.acknowledge();
    }

    private CheckDepositWithDelayCommand toCommand(DepositCheckMessage message) {
        return new CheckDepositWithDelayCommand(
                new ExchangeId(message.exchangeId()),
                new Count(message.count()),
                message.direction()
        );
    }

    private record DepositCheckMessage(
            String exchangeId,
            int count,
            Direction direction
    ) {}
}
