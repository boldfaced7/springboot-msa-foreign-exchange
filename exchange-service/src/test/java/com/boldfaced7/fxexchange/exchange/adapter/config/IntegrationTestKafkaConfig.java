package com.boldfaced7.fxexchange.exchange.adapter.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Profile("integration-test")
@TestConfiguration
public class IntegrationTestKafkaConfig {

    @Value("${kafka.topic.scheduler.scheduler-topic}")
    private String schedulerTopic;

    @Value("${kafka.topic.exchange.deposit-check-topic}")
    private String depositCheckTopic;

    @Value("${kafka.topic.exchange.withdrawal-check-topic}")
    private String withdrawalCheckTopic;

    @Value("${kafka.topic.fx-account.withdrawal-cancel-request-topic}")
    private String fxWithdrawalCancelRequestTopic;

    @Value("${kafka.topic.krw-account.withdrawal-cancel-request-topic}")
    private String krwWithdrawalCancelRequestTopic;

    @Value("${kafka.topic.fx-account.withdrawal-cancel-response-topic}")
    private String fxWithdrawalCancelResponseTopic;

    @Value("${kafka.topic.krw-account.withdrawal-cancel-response-topic}")
    private String krwWithdrawalCancelResponseTopic;


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumerFactory);

        factory.setBatchListener(false);
        factory.setConcurrency(1);
        factory.getContainerProperties().setPollTimeout(1000);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }

    @Bean
    public NewTopic schedulerTopic() {
        return TopicBuilder.name(schedulerTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic depositCheckTopic() {
        return TopicBuilder.name(depositCheckTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic withdrawalCheckTopic() {
        return TopicBuilder.name(withdrawalCheckTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic fxWithdrawalCancelRequestTopic() {
        return TopicBuilder.name(fxWithdrawalCancelRequestTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic krwWithdrawalCancelRequestTopic() {
        return TopicBuilder.name(krwWithdrawalCancelRequestTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic fxWithdrawalCancelResponseTopic() {
        return TopicBuilder.name(fxWithdrawalCancelResponseTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic krwWithdrawalCancelResponseTopic() {
        return TopicBuilder.name(krwWithdrawalCancelResponseTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
