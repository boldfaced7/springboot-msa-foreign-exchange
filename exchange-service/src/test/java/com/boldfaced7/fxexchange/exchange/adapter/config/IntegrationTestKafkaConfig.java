package com.boldfaced7.fxexchange.exchange.adapter.config;

import com.boldfaced7.fxexchange.exchange.adapter.out.property.KafkaExchangeProperties;
import com.boldfaced7.fxexchange.exchange.adapter.out.property.KafkaFxAccountProperties;
import com.boldfaced7.fxexchange.exchange.adapter.out.property.KafkaKrwAccountProperties;
import com.boldfaced7.fxexchange.exchange.adapter.out.property.KafkaSchedulerProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Profile("!application-test")
@Configuration
@RequiredArgsConstructor
public class IntegrationTestKafkaConfig {

    private final KafkaExchangeProperties exchange;
    private final KafkaFxAccountProperties fxAccount;
    private final KafkaKrwAccountProperties krwAccount;
    private final KafkaSchedulerProperties scheduler;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory
    ) {
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
        return TopicBuilder.name(scheduler.schedulerTopic())
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic depositCheckTopic() {
        return TopicBuilder.name(exchange.depositCheckTopic())
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic withdrawalCheckTopic() {
        return TopicBuilder.name(exchange.withdrawalCheckTopic())
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic fxWithdrawalCancelRequestTopic() {
        return TopicBuilder.name(fxAccount.withdrawalCancelRequestTopic())
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic krwWithdrawalCancelRequestTopic() {
        return TopicBuilder.name(krwAccount.withdrawalCancelRequestTopic())
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic fxWithdrawalCancelResponseTopic() {
        return TopicBuilder.name(fxAccount.withdrawalCancelResponseTopic())
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic krwWithdrawalCancelResponseTopic() {
        return TopicBuilder.name(krwAccount.withdrawalCancelResponseTopic())
                .partitions(1)
                .replicas(1)
                .build();
    }
}
