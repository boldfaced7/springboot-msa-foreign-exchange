package com.boldfaced7.fxexchange.exchange.adapter.config;

import com.boldfaced7.fxexchange.exchange.adapter.out.property.KafkaExchangeProperties;
import com.boldfaced7.fxexchange.exchange.adapter.out.property.KafkaFxAccountProperties;
import com.boldfaced7.fxexchange.exchange.adapter.out.property.KafkaKrwAccountProperties;
import com.boldfaced7.fxexchange.exchange.adapter.out.property.KafkaSchedulerProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Profile("dev")
@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaExchangeProperties exchange;
    private final KafkaFxAccountProperties fxAccount;
    private final KafkaKrwAccountProperties krwAccount;
    private final KafkaSchedulerProperties scheduler;

    private final KafkaProperties kafkaProperties;

    /**
     * Kafka Admin 설정
     * 토픽 생성, 삭제, 설정 변경 등의 관리 작업을 수행
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        return new KafkaAdmin(configs);
    }

    /**
     * 프로듀서 팩토리 설정
     * 메시지 생산과 관련된 모든 설정을 포함
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
    }

    /**
     * 컨슈머 팩토리 설정
     * 메시지 소비와 관련된 모든 설정을 포함
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties());
    }

    /**
     * Kafka 리스너 컨테이너 팩토리 설정
     * 메시지 리스닝과 처리에 관련된 설정을 포함
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate) {
        
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        // 수동 커밋 모드 설정 - 메시지 처리 보장
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        // 동시 처리 스레드 수 설정 - 성능 최적화
        factory.setConcurrency(3);
        // 배치 처리 설정 - 성능 최적화
        factory.setBatchListener(true);
        // 폴링 타임아웃 설정 - 메시지 폴링 대기 시간
        factory.getContainerProperties().setPollTimeout(3000);
        
        // 에러 핸들링 설정
        // DLT(Dead Letter Topic)로 실패한 메시지 전송
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        // 재시도 정책 설정 - 1초 간격으로 3번 재시도
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            recoverer,
            new FixedBackOff(1000L, 3)
        );
        // 재시도 가능한 예외 설정
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        factory.setCommonErrorHandler(errorHandler);
        
        return factory;
    }

    /**
     * Kafka 템플릿 설정
     * 메시지 생성을 위한 템플릿 제공
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic schedulerTopic() {
        return TopicBuilder.name(scheduler.schedulerTopic())
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic depositCheckTopic() {
        return TopicBuilder.name(exchange.depositCheckTopic())
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic withdrawalCheckTopic() {
        return TopicBuilder.name(exchange.withdrawalCheckTopic())
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic fxWithdrawalCancelRequestTopic() {
        return TopicBuilder.name(fxAccount.withdrawalCancelRequestTopic())
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic krwWithdrawalCancelRequestTopic() {
        return TopicBuilder.name(krwAccount.withdrawalCancelRequestTopic())
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic fxWithdrawalCancelResponseTopic() {
        return TopicBuilder.name(fxAccount.withdrawalCancelResponseTopic())
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic krwWithdrawalCancelResponseTopic() {
        return TopicBuilder.name(krwAccount.withdrawalCancelResponseTopic())
                .partitions(3)
                .replicas(3)
                .build();
    }

}
