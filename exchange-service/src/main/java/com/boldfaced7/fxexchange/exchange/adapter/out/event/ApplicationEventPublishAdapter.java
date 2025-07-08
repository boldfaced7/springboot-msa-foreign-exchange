package com.boldfaced7.fxexchange.exchange.adapter.out.event;

import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.model.EventDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationEventPublishAdapter implements PublishEventPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(EventDomain eventDomain) {
        List<DomainEvent> pulled = eventDomain.pullEvents();
        pulled.forEach(e -> log.info("{}", e));
        pulled.forEach(applicationEventPublisher::publishEvent);
    }

}
