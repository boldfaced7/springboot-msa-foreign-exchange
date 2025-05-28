package com.boldfaced7.fxexchange.exchange.application.port.out;

import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;

import java.util.List;

public interface PublishExchangeEventPort {
    void publish(List<DomainEvent> events);
}
