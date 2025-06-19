package com.boldfaced7.fxexchange.exchange.adapter.test;

import com.boldfaced7.fxexchange.exchange.application.port.out.PublishExchangeEventPort;
import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

@Slf4j
@TestComponent
@RequiredArgsConstructor
public class PublishExchangeEventPortForTest implements PublishExchangeEventPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(List<DomainEvent> events) {
        log.info("Publishing events: {}", events);
        events.forEach(applicationEventPublisher::publishEvent);
    }
}
