package com.boldfaced7.fxexchange.exchange.adapter.in.messaging;

import com.boldfaced7.fxexchange.common.MessagingAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.in.CompleteWithdrawalCancelCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.CompleteWithdrawalCancelUseCase;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalCancelId;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@MessagingAdapter
@Component
@RequiredArgsConstructor
public class FxWithdrawalCancelKafkaConsumer {

    private final CompleteWithdrawalCancelUseCase completeWithdrawalCancelUseCase;
    private final MessageDeserializer messageDeserializer;

    @KafkaListener(
            topics  = "${kafka.topic.fx-withdrawal-cancel}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message, Acknowledgment ack) {
        var cancelMessage = messageDeserializer.deserializeMessage(message, WithdrawalCancelMessage.class);
        var command = toCommand(cancelMessage);
        completeWithdrawalCancelUseCase.completeWithdrawalCancel(command);
        ack.acknowledge();
    }

    private CompleteWithdrawalCancelCommand toCommand(WithdrawalCancelMessage message) {
        return new CompleteWithdrawalCancelCommand(
                new ExchangeId(message.exchangeId()),
                new WithdrawalCancelId(message.withdrawalCancelId),
                Direction.SELL
        );
    }

    private record WithdrawalCancelMessage(
            String exchangeId,
            String withdrawalCancelId
    ) {}
}
