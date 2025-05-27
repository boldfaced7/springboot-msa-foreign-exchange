package com.boldfaced7.fxexchange.exchange.application.port.out;

import java.util.List;

public interface PublishExchangeEventPort {
    void publish(List<Object> events);
}
