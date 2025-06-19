package com.boldfaced7.fxexchange.exchange.adapter.in.messaging;

import com.boldfaced7.fxexchange.common.MessagingAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckWithdrawalWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckWithdrawalWithDelayUseCase;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@MessagingAdapter
@Component
@RequiredArgsConstructor
public class WithdrawalCheckKafkaConsumer {

    private final CheckWithdrawalWithDelayUseCase checkWithdrawalWithDelayUseCase;

    @KafkaListener(
            topics = "${kafka.topic.exchange.withdrawal-check-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message, Acknowledgment ack) {
        log.info("[WithdrawalCheck] 출금 확인 메시지 수신 - message: {}", message);
        
        try {
            var checkMessage = MessageDeserializer.deserializeMessage(message, WithdrawalCheckMessage.class);
            var command = toCommand(checkMessage);
            
            log.info("[WithdrawalCheck] 출금 확인 처리 시작 - exchangeId: {}, count: {}, direction: {}", 
                    command.exchangeId().value(), command.count().value(), command.direction());
            
            checkWithdrawalWithDelayUseCase.checkWithdrawalWithDelay(command);
            
            log.info("[WithdrawalCheck] 출금 확인 처리 완료 - exchangeId: {}", command.exchangeId().value());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[WithdrawalCheck] 출금 확인 처리 실패 - message: {}, error: {}", 
                    message, e.getMessage(), e);
            throw e;
        }
    }

    private CheckWithdrawalWithDelayCommand toCommand(WithdrawalCheckMessage message) {
        return new CheckWithdrawalWithDelayCommand(
                new ExchangeId(message.exchangeId()),
                new Count(message.count()),
                message.direction()
        );
    }

    public record WithdrawalCheckMessage(
            String exchangeId,
            int count,
            Direction direction
    ) {
        public WithdrawalCheckMessage {
            log.debug("[WithdrawalCheck] 메시지 객체 생성 - exchangeId: {}, count: {}, direction: {}", 
                    exchangeId, count, direction);
        }
    }
}
