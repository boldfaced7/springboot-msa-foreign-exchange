package com.boldfaced7.fxexchange.exchange.adapter.out.messaging;

import com.boldfaced7.fxexchange.common.MessagingAdapter;
import com.boldfaced7.fxexchange.exchange.adapter.out.messaging.util.CheckSchedulingTopicMapper;
import com.boldfaced7.fxexchange.exchange.adapter.out.messaging.util.MessageSerializer;
import com.boldfaced7.fxexchange.exchange.adapter.out.property.KafkaSchedulerProperties;
import com.boldfaced7.fxexchange.exchange.application.port.out.external.ScheduleCheckRequestPort;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.enums.TransactionType;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Slf4j
@MessagingAdapter
@RequiredArgsConstructor
public class CheckSchedulingKafkaPublisher implements ScheduleCheckRequestPort {

    private final KafkaSchedulerProperties schedulerProperties;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final CheckSchedulingTopicMapper checkSchedulingTopicMapper;

    @Override
    public void scheduleCheckRequest(
            ExchangeId exchangeId,
            Duration delay,
            Count count,
            Direction direction,
            TransactionType transactionType
    ) {
        log.info("[CheckScheduling] 스케줄링 요청 시작 - exchangeId: {}, delay: {}, count: {}, direction: {}, checkType: {}", 
                exchangeId.value(), delay.toMillis(), count.value(), direction, transactionType);
        
        var request = new CheckSchedulingRequest(exchangeId, count, direction);
        var payload = MessageSerializer.serializeMessage(request);
        var replyTopic = checkSchedulingTopicMapper.getTopic(transactionType);


        var kafkaMessage = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, schedulerProperties.schedulerTopic())
                .setHeader(KafkaHeaders.REPLY_TOPIC, replyTopic)
                .setHeader(schedulerProperties.scheduledTimeMillisHeader(), delay.toMillis())
                .build();

        log.info(
                "[CheckScheduling] 스케줄링 토픽 - topic: {}, replyTopic: {}",
                kafkaMessage.getHeaders().get(KafkaHeaders.TOPIC),
                kafkaMessage.getHeaders().get(KafkaHeaders.REPLY_TOPIC)
        );

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(kafkaMessage);
        future.thenAccept(result -> {
            var metadata = result.getRecordMetadata();
            log.info("[CheckScheduling] 스케줄링 요청 완료 - exchangeId: {}, topic: {}, partition: {}, offset: {}", 
                    exchangeId.value(), 
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset());
        }).exceptionally(throwable -> {
            log.error("[CheckScheduling] 스케줄링 요청 실패 - exchangeId: {}, error: {}", 
                    exchangeId.value(), throwable.getMessage(), throwable);
            throw new RuntimeException("스케줄링 요청 실패", throwable);
        });
    }

    private record CheckSchedulingRequest(
            String exchangeId,
            int count,
            Direction direction
    ) {
        public CheckSchedulingRequest(
                ExchangeId exchangeId,
                Count count,
                Direction direction
        ) {
            this(exchangeId.value(), count.value(), direction);
            log.debug("[CheckScheduling] 요청 객체 생성 - exchangeId: {}, count: {}, direction: {}", 
                    exchangeId.value(), count.value(), direction);
        }
    }

}