package com.boldfaced7.fxexchange.exchange.adapter.in.messaging;

import com.boldfaced7.fxexchange.common.MessagingAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckWithdrawalWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckWithdrawalWithDelayUseCase;
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
public class WithdrawalCheckKafkaConsumer {

    private final CheckWithdrawalWithDelayUseCase checkWithdrawalWithDelayUseCase;
    private final MessageDeserializer messageDeserializer;

    @KafkaListener(
            topics = "${kafka.topic.withdrawal-check}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message, Acknowledgment ack) {
        var checkMessage = messageDeserializer.deserializeMessage(message, WithdrawalCheckMessage.class);
        var command = toCommand(checkMessage);
        checkWithdrawalWithDelayUseCase.checkWithdrawalWithDelay(command);
        ack.acknowledge();
    }

    private CheckWithdrawalWithDelayCommand toCommand(WithdrawalCheckMessage message) {
        return new CheckWithdrawalWithDelayCommand(
                new ExchangeId(message.exchangeId()),
                new Count(message.count()),
                message.direction()
        );
    }

    private record WithdrawalCheckMessage(
            String exchangeId,
            int count,
            Direction direction
    ) {}
}
