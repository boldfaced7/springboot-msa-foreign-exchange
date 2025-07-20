package com.boldfaced7.fxexchange.exchange.application.util;

import com.boldfaced7.fxexchange.exchange.adapter.aop.DistributedLockAspect;
import com.boldfaced7.fxexchange.exchange.adapter.config.*;
import com.boldfaced7.fxexchange.exchange.adapter.in.messaging.DepositCheckKafkaConsumer;
import com.boldfaced7.fxexchange.exchange.adapter.in.messaging.WithdrawalCancelKafkaConsumer;
import com.boldfaced7.fxexchange.exchange.adapter.in.messaging.WithdrawalCheckKafkaConsumer;
import com.boldfaced7.fxexchange.exchange.adapter.out.cache.RedisExchangeIdIndexRepository;
import com.boldfaced7.fxexchange.exchange.adapter.out.cache.RedisExchangeRequestRepository;
import com.boldfaced7.fxexchange.exchange.adapter.out.property.*;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.apache.kafka.clients.admin.NewTopic;
import org.aspectj.lang.ProceedingJoinPoint;
import org.awaitility.Awaitility;
import org.awaitility.core.ThrowingRunnable;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@EnableAutoConfiguration(exclude = {
        // JPA 관련
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,

        // Redis 관련
        org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class,

        // Kafka 관련
        org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class
})
public class ApplicationTestSupport {

    @MockitoBean DistributedLockAspect distributedLockAspect;

    @MockitoBean ConfigurationPropertiesConfig configurationPropertiesConfig;
    @MockitoBean KafkaConfig kafkaConfig;
    @MockitoBean RedisConfig redisConfig;
    @MockitoBean RedissonConfig redissonConfig;
    @MockitoBean WebClientConfig webClientConfig;

    @MockitoBean RedisExchangeIdIndexRepository redisExchangeIdIndexRepository;
    @MockitoBean RedisExchangeRequestRepository redisExchangeRequestRepository;
    @MockitoBean StringRedisTemplate stringRedisTemplate;
    @MockitoBean RedisConnectionFactory redisConnectionFactory;
    @MockitoBean RedissonClient redissonClient;

    @MockitoBean(name = "fxWebClient")  WebClient fxWebClient;
    @MockitoBean(name = "krwWebClient") WebClient krwlWebClient;

    @MockitoBean(name = "fxCircuitBreaker") CircuitBreaker fxCircuitBreaker;
    @MockitoBean(name = "krwCircuitBreaker") CircuitBreaker krwCircuitBreaker;


    @MockitoBean KafkaAdmin kafkaAdmin;
    @MockitoBean ProducerFactory<String, String> producerFactory;
    @MockitoBean ConsumerFactory<String, String> consumerFactory;
    @MockitoBean ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory;
    @MockitoBean KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean(name = "schedulerTopic") NewTopic schedulerTopic;
    @MockitoBean(name = "depositCheckTopic") NewTopic depositCheckTopic;
    @MockitoBean(name = "withdrawalCheckTopic") NewTopic withdrawalCheckTopic;
    @MockitoBean(name = "fxWithdrawalCancelRequestTopic") NewTopic fxWithdrawalCancelRequestTopic;
    @MockitoBean(name = "krwWithdrawalCancelRequestTopic") NewTopic krwWithdrawalCancelRequestTopic;
    @MockitoBean(name = "fxWithdrawalCancelResponseTopic") NewTopic fxWithdrawalCancelResponseTopic;
    @MockitoBean(name = "krwWithdrawalCancelResponseTopic") NewTopic krwWithdrawalCancelResponseTopic;

    @MockitoBean DepositCheckKafkaConsumer depositCheckKafkaConsumer;
    @MockitoBean WithdrawalCancelKafkaConsumer withdrawalCancelKafkaConsumer;
    @MockitoBean WithdrawalCheckKafkaConsumer withdrawalCheckKafkaConsumer;

    @MockitoBean ExchangeServiceProperties exchangeServiceProperties;
    @MockitoBean KafkaExchangeProperties kafkaExchangeProperties;
    @MockitoBean KafkaFxAccountProperties kafkaFxAccountProperties;
    @MockitoBean KafkaKrwAccountProperties krwAccountProperties;
    @MockitoBean KafkaSchedulerProperties kafkaSchedulerProperties;
    @MockitoBean WebClientBaseUrlProperties webClientBaseUrlProperties;

    public static void verifySaga(ThrowingRunnable runnable) {
        Awaitility.await()
                .pollDelay(Duration.ZERO)
                .pollInterval(Duration.ofMillis(1000))
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(runnable);
    }

    public void stubAop() {
        try {
            doAnswer(invocation ->
                    ((ProceedingJoinPoint) invocation.getArgument(0)).proceed()
            ).when(distributedLockAspect).lock(any());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}   
