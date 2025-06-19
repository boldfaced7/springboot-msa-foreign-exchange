package com.boldfaced7.fxexchange.exchange.adapter.out.messaging;

import com.boldfaced7.fxexchange.common.MessagingAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.out.ScheduleCheckRequestPort;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.enums.TransactionCheckType;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final CheckSchedulingTopicMapper checkSchedulingTopicMapper;

    @Value("${kafka.topic.scheduler.scheduler-topic}")
    private String schedulerTopic;

    @Value("${kafka.header.scheduler.scheduled-time-millis}")
    private String scheduledTimeMillisHeader;

    @Override
    public void scheduleCheckRequest(
            ExchangeId exchangeId,
            Duration delay,
            Count count,
            Direction direction,
            TransactionCheckType transactionCheckType
    ) {
        log.info("[CheckScheduling] 스케줄링 요청 시작 - exchangeId: {}, delay: {}, count: {}, direction: {}, checkType: {}", 
                exchangeId.value(), delay.toMillis(), count.value(), direction, transactionCheckType);
        
        var request = new CheckSchedulingRequest(exchangeId, count, direction);
        var payload = MessageSerializer.serializeMessage(request);
        var replyTopic = checkSchedulingTopicMapper.getTopic(transactionCheckType);

        var kafkaMessage = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, schedulerTopic)
                .setHeader(KafkaHeaders.REPLY_TOPIC, replyTopic)
                .setHeader(scheduledTimeMillisHeader, delay.toMillis())
                .build();

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