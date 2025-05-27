package com.boldfaced7.fxexchange.exchange.application.service.util;

import com.boldfaced7.fxexchange.exchange.application.port.out.PublishExchangeEventPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangeEventPublisherImpl implements ExchangeEventPublisher {

    private final PublishExchangeEventPort publishExchangeEventPort;

    @Override
    public void publishEvents(ExchangeRequest requested) {
        var raised = requested.pullEvents();
        publishExchangeEventPort.publish(raised);
    }
}
