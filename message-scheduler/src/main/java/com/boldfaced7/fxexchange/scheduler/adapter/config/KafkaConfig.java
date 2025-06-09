package com.boldfaced7.fxexchange.scheduler.adapter.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

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
        Map<String, Object> configProps = new HashMap<>(kafkaProperties.buildProducerProperties());

        // 추가 프로듀서 설정
        // 멱등성 보장 - 중복 메시지 방지
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        // 동시 요청 수 제한 - 네트워크 부하 조절
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        // 배치 처리 대기 시간 설정 - 배치 처리 효율성 향상
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        // 압축 타입 설정 - 네트워크 대역폭 절약
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        // 타임아웃 설정 - 네트워크 장애 대응
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        // 최대 블록 시간 설정 - 버퍼 가득 찼을 때 대기 시간
        configProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 60000);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * 컨슈머 팩토리 설정
     * 메시지 소비와 관련된 모든 설정을 포함
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>(kafkaProperties.buildConsumerProperties());

        // 추가 컨슈머 설정
        // 수동 커밋 모드 사용 - 메시지 처리 보장
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        // 최대 폴링 간격 설정 - 컨슈머 장애 감지
        configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        // 최대 폴링 레코드 수 제한 - 메모리 사용량 제어
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        // 세션 타임아웃 설정 - 컨슈머 장애 감지
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
        // 하트비트 간격 설정 - 컨슈머 상태 모니터링
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        // 그룹 인스턴스 ID 설정 - 컨슈머 그룹 내 유니크한 식별자
        configProps.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, "exchange-consumer-1");
        // 오프셋 리셋 정책 설정 - earliest: 처음부터, latest: 최신부터
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(configProps);
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

    /**
     * 기본 토픽 생성
     * 파티션 수: 3, 복제 팩터: 3
     */
    @Bean
    public NewTopic exchangeTopic() {
        return new NewTopic("exchange-topic", 3, (short) 3);
    }

    /**
     * Dead Letter Topic 생성
     * 처리 실패한 메시지를 저장하는 토픽
     * 실무에서는 실패한 메시지의 재처리나 모니터링에 사용
     */
    @Bean
    public NewTopic exchangeDltTopic() {
        return new NewTopic("exchange-topic.DLT", 3, (short) 3);
    }
}
