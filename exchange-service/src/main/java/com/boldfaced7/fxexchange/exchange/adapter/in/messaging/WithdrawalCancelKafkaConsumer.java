package com.boldfaced7.fxexchange.exchange.adapter.in.messaging;

import com.boldfaced7.fxexchange.common.MessagingAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.in.CompleteWithdrawalCancelCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.CompleteWithdrawalCancelUseCase;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.cancel.WithdrawalCancelId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@MessagingAdapter
@Component
@RequiredArgsConstructor
public class WithdrawalCancelKafkaConsumer {

    private final CompleteWithdrawalCancelUseCase completeWithdrawalCancelUseCase;

    @KafkaListener(
            topics  = "${kafka.fx-account.withdrawal-cancel-response-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void fromFxAccount(String message, Acknowledgment ack) {
        fromAccount(message, Direction.SELL, ack);
    }

    @KafkaListener(
            topics  = "${kafka.krw-account.withdrawal-cancel-response-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void fromKrwAccount(String message, Acknowledgment ack) {
        fromAccount(message, Direction.BUY, ack);
    }

    private void fromAccount(String payload, Direction direction, Acknowledgment ack) {
        log.info("[WithdrawalCancel] 출금 취소 메시지 수신 - message: {}", payload);

        try {
            var cancelMessage = MessageDeserializer.deserializeMessage(payload, WithdrawalCancelMessage.class);
            var command = toCommand(cancelMessage, direction);

            log.info("[WithdrawalCancel] 출금 취소 처리 시작 - exchangeId: {}, withdrawalCancelId: {}",
                    command.exchangeId().value(), command.withdrawalCancelId().value());

            completeWithdrawalCancelUseCase.completeWithdrawalCancel(command);

            log.info("[WithdrawalCancel] 출금 취소 처리 완료 - exchangeId: {}, withdrawalCancelId: {}",
                    command.exchangeId().value(), command.withdrawalCancelId().value());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[WithdrawalCancel] 출금 취소 처리 실패 - message: {}, error: {}",
                    payload, e.getMessage(), e);
            throw e;
        }

    }

    private CompleteWithdrawalCancelCommand toCommand(WithdrawalCancelMessage message, Direction direction) {
        return new CompleteWithdrawalCancelCommand(
                new WithdrawalCancelId(message.withdrawalCancelId()),
                new ExchangeId(message.exchangeId()),
                direction
        );
    }

    public record WithdrawalCancelMessage(
            String exchangeId,
            String withdrawalCancelId
    ) {}
}
