package com.boldfaced7.fxexchange.exchange.adapter.out.messaging;

import com.boldfaced7.fxexchange.common.MessagingAdapter;
import com.boldfaced7.fxexchange.exchange.adapter.out.messaging.util.MessageSerializer;
import com.boldfaced7.fxexchange.exchange.adapter.out.messaging.util.WithdrawalCancelTopicMapper;
import com.boldfaced7.fxexchange.exchange.application.port.out.cancel.CancelWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;

import java.util.concurrent.CompletableFuture;

@Slf4j
@MessagingAdapter
@RequiredArgsConstructor
public class WithdrawalCancelKafkaPublisher implements CancelWithdrawalPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final WithdrawalCancelTopicMapper topicMapper;

    @Override
    public void cancelWithdrawal(ExchangeId exchangeId, Direction direction) {
        log.info("[WithdrawalCancel] 출금 취소 요청 시작 - exchangeId: {}, direction: {}", 
                exchangeId.value(), direction);
        
        var requestTopic = topicMapper.getRequestTopic(direction);
        var responseTopic = topicMapper.getResponseTopic(direction);
        var message = new WithdrawalCancelRequest(exchangeId);
        var serialized = MessageSerializer.serializeMessage(message);

        var kafkaMessage = MessageBuilder.withPayload(serialized)
                .setHeader(KafkaHeaders.TOPIC, requestTopic)
                .setHeader(KafkaHeaders.REPLY_TOPIC, responseTopic)
                .build();
        
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(kafkaMessage);
        future.thenAccept(result -> {
            var metadata = result.getRecordMetadata();
            log.info("[WithdrawalCancel] 출금 취소 요청 완료 - exchangeId: {}, topic: {}, partition: {}, offset: {}", 
                    exchangeId.value(),
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset());
        }).exceptionally(throwable -> {
            log.error("[WithdrawalCancel] 출금 취소 요청 실패 - exchangeId: {}, direction: {}, error: {}", 
                    exchangeId.value(), direction, throwable.getMessage(), throwable);
            throw new RuntimeException("출금 취소 요청 실패", throwable);
        });
    }

    public record WithdrawalCancelRequest(
            String exchangeId
    ) {
        public WithdrawalCancelRequest(ExchangeId exchangeId) {
            this(exchangeId.value());
            log.debug("[WithdrawalCancel] 요청 객체 생성 - exchangeId: {}", exchangeId.value());
        }
    }
}