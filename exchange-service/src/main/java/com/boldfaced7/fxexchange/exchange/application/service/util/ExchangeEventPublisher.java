package com.boldfaced7.fxexchange.exchange.application.service.util;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;

public interface ExchangeEventPublisher {
    void publishEvents(ExchangeRequest requested);
}
