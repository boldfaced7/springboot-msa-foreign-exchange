package com.boldfaced7.fxexchange.exchange.adapter.test;

import com.boldfaced7.fxexchange.exchange.adapter.test.account.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestPortConfiguration {

    // WARNING MESSAGE
    @Bean
    public NoOpSendWarningMessagePort noOpSendWarningMessagePort() {
        return new NoOpSendWarningMessagePort();
    }

    // EVENT
    @Bean
    public PublishExchangeEventPortForTest publishExchangeEventPortForTest(
            ApplicationEventPublisher applicationEventPublisher
    ) {
        return new PublishExchangeEventPortForTest(applicationEventPublisher);
    }

    // PERSISTENCE
    @Bean
    public ExchangeRequestPersistenceAdapterForTest exchangeRequestPersistenceAdapterForTest() {
        return new ExchangeRequestPersistenceAdapterForTest();
    }

    @Bean
    public ExchangeStateLogPersistenceAdapterForTest exchangeStateLogPersistenceAdapterForTest() {
        return new ExchangeStateLogPersistenceAdapterForTest();
    }

    // BUY
    @Bean
    public LoadDepositResultPortForTest loadDepositResultPortForTest() {
        return new LoadDepositResultPortForTest();
    }

    @Bean
    public LoadWithdrawalResultPortForTest loadWithdrawalResultPortForTest() {
        return new LoadWithdrawalResultPortForTest();
    }


    @Bean
    public RequestDepositPortForTest requestDepositPortForTest() {
        return new RequestDepositPortForTest();
    }

    @Bean
    public RequestWithdrawalPortForTest requestWithdrawalPortForTest() {
        return new RequestWithdrawalPortForTest();
    }


    @Bean
    public SendDepositCheckRequestPortForTest sendDepositCheckRequestPortForTest() {
        return new SendDepositCheckRequestPortForTest();
    }

    @Bean
    public SendWithdrawalCheckRequestPortForTest sendWithdrawalCheckRequestPortForTest() {
        return new SendWithdrawalCheckRequestPortForTest();
    }

    @Bean
    public UndoWithdrawalPortForTest undoWithdrawalPortForTest() {
        return new UndoWithdrawalPortForTest();
    }
}
