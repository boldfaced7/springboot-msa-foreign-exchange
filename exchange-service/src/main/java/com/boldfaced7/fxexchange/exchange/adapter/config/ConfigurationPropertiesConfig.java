package com.boldfaced7.fxexchange.exchange.adapter.config;

import com.boldfaced7.fxexchange.exchange.adapter.out.property.*;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RetryPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        ExchangeServiceProperties.class,
        KafkaExchangeProperties.class,
        KafkaFxAccountProperties.class,
        KafkaKrwAccountProperties.class,
        KafkaSchedulerProperties.class,
        WebClientBaseUrlProperties.class
})
public class ConfigurationPropertiesConfig {

    @Bean
    RetryPolicy exchangeProperties(
            ExchangeServiceProperties exchangeServiceProperties
    ) {
        return new RetryPolicy(
                exchangeServiceProperties.maxCountCheck(),
                exchangeServiceProperties.delaySecond()
        );
    }
}