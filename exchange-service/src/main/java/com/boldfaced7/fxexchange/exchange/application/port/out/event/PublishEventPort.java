package com.boldfaced7.fxexchange.exchange.application.port.out.event;

import com.boldfaced7.fxexchange.exchange.domain.model.EventDomain;

public interface PublishEventPort {
    void publish(EventDomain eventDomain);
}
