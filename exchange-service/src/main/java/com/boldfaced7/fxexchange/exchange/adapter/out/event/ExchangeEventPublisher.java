package com.boldfaced7.fxexchange.exchange.adapter.out.event;

import com.boldfaced7.fxexchange.exchange.application.port.out.PublishExchangeEventPort;
import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class ExchangeEventPublisher implements PublishExchangeEventPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(List<DomainEvent> events) {
        events.forEach(applicationEventPublisher::publishEvent);
    }
}
