package com.boldfaced7.fxexchange.exchange.adapter.in.messaging;

import com.boldfaced7.fxexchange.common.MessagingAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckDepositWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckDepositWithDelayUseCase;
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
public class DepositCheckKafkaConsumer {

    private final CheckDepositWithDelayUseCase checkDepositWithDelayUseCase;

    @KafkaListener(
            topics = "${kafka.exchange.deposit-check-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message, Acknowledgment ack) {
        log.info("[DepositCheck] 입금 확인 메시지 수신 - message: {}", message);
        
        try {
            var checkMessage = MessageDeserializer.deserializeMessage(message, DepositCheckMessage.class);
            var command = toCommand(checkMessage);
            
            log.info("[DepositCheck] 입금 확인 처리 시작 - exchangeId: {}, count: {}, direction: {}", 
                    command.exchangeId().value(), command.count().value(), command.direction());
            
            checkDepositWithDelayUseCase.checkDepositWithDelay(command);
            
            log.info("[DepositCheck] 입금 확인 처리 완료 - exchangeId: {}", command.exchangeId().value());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[DepositCheck] 입금 확인 처리 실패 - message: {}, error: {}", 
                    message, e.getMessage(), e);
            throw e;
        }
    }

    private CheckDepositWithDelayCommand toCommand(DepositCheckMessage message) {
        return new CheckDepositWithDelayCommand(
                new ExchangeId(message.exchangeId()),
                new Count(message.count()),
                message.direction()
        );
    }

    public record DepositCheckMessage(
            String exchangeId,
            int count,
            Direction direction
    ) {
        public DepositCheckMessage {
            log.debug("[DepositCheck] 메시지 객체 생성 - exchangeId: {}, count: {}, direction: {}", 
                    exchangeId, count, direction);
        }
    }
}
