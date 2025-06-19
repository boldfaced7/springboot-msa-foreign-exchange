package com.boldfaced7.fxexchange.exchange.application.config;

import com.boldfaced7.fxexchange.exchange.adapter.test.ExchangeRequestPersistenceAdapterForTest;
import com.boldfaced7.fxexchange.exchange.adapter.test.ExchangeStateLogPersistenceAdapterForTest;
import com.boldfaced7.fxexchange.exchange.adapter.test.PublishExchangeEventPortForTest;
import com.boldfaced7.fxexchange.exchange.adapter.test.SendWarningMessagePortForTest;
import com.boldfaced7.fxexchange.exchange.adapter.test.account.*;
import com.boldfaced7.fxexchange.exchange.application.port.out.*;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Profile("application-test")
@TestConfiguration
public class ApplicationTestPortConfig {

    // WARNING MESSAGE
    @Bean
    @Primary
    public SendWarningMessagePort SendWarningMessagePort() {
        return new SendWarningMessagePortForTest();
    }

    // EVENT
    @Bean
    @Primary
    public PublishExchangeEventPort publishExchangeEventPort(
            ApplicationEventPublisher applicationEventPublisher
    ) {
        return new PublishExchangeEventPortForTest(applicationEventPublisher);
    }

    // PERSISTENCE
    @Bean
    @Primary
    public ExchangeRequestPersistenceAdapterForTest exchangeRequestPersistenceAdapterForTest() {
        return new ExchangeRequestPersistenceAdapterForTest();
    }

    @Bean
    @Primary
    public ExchangeStateLogPersistenceAdapterForTest exchangeStateLogPersistenceAdapterForTest() {
        return new ExchangeStateLogPersistenceAdapterForTest();
    }

//    @Bean
//    @Primary
//    public LoadExchangeRequestPort loadExchangeRequestPort() {
//        return new ExchangeRequestPersistenceAdapterForTest();
//    }
//
//    @Bean
//    @Primary
//    public SaveExchangeRequestPort saveExchangeRequestPort() {
//        return new ExchangeRequestPersistenceAdapterForTest();
//    }
//
//    @Bean
//    @Primary
//    public UpdateExchangeRequestPort updateExchangeRequestPort() {
//        return new ExchangeRequestPersistenceAdapterForTest();
//    }
//
//    @Bean
//    @Primary
//    public SaveExchangeStateLogPort saveExchangeStateLogPort() {
//        return new ExchangeStateLogPersistenceAdapterForTest();
//    }

    // BUY
    @Bean
    @Primary
    public LoadDepositResultPort loadDepositResultPort() {
        return new LoadDepositResultPortForTest();
    }

    @Bean
    @Primary
    public LoadWithdrawalResultPort loadWithdrawalResultPort() {
        return new LoadWithdrawalResultPortForTest();
    }


    @Bean
    @Primary
    public RequestDepositPort requestDepositPort() {
        return new RequestDepositPortForTest();
    }

    @Bean
    @Primary
    public RequestWithdrawalPort requestWithdrawalPort() {
        return new RequestWithdrawalPortForTest();
    }

    @Bean
    @Primary
    public ScheduleCheckRequestPort scheduleCheckRequestPort() {
        return new ScheduleCheckRequestPortForTest();
    }

    @Bean
    @Primary
    public CancelWithdrawalPort cancelWithdrawalPort() {
        return new CancelWithdrawalPortForTest();
    }

    private static <T> Map<Direction, T> toDirectionMap(T t) {
        return Arrays.stream(Direction.values()).collect(Collectors.toMap(
                Function.identity(),
                direction -> t
        ));
    }

    // LoadDepositResultPorts
    @Bean
    @Primary
    public Map<Direction, LoadDepositResultPort> loadDepositResultPorts(
            LoadDepositResultPort loadDepositResultPort
    ) {
        return toDirectionMap(loadDepositResultPort);
    }

    // LoadWithdrawalResultPorts
    @Bean
    @Primary
    public Map<Direction, LoadWithdrawalResultPort> loadWithdrawalResultPorts(
            LoadWithdrawalResultPort loadWithdrawalResultPort
    ) {
        return toDirectionMap(loadWithdrawalResultPort);
    }

    // RequestDepositPorts
    @Bean
    @Primary
    public Map<Direction, RequestDepositPort> requestDepositPorts(
            RequestDepositPort requestDepositPort
    ) {
        return toDirectionMap(requestDepositPort);
    }

    // RequestWithdrawalPorts
    @Bean
    @Primary
    public Map<Direction, RequestWithdrawalPort> requestWithdrawalPorts(
            RequestWithdrawalPort requestWithdrawalPort
    ) {
        return toDirectionMap(requestWithdrawalPort);
    }
//
//    @Bean
//    @Primary
//    public DataSource dataSource() {
//        return new EmbeddedDatabaseBuilder()
//                .setType(EmbeddedDatabaseType.H2)
//                .build();
//    }


}
