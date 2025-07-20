package com.boldfaced7.fxexchange.exchange.application.service.saga.exchange.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.exchange.SaveExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.service.saga.exchange.CreateExchangeRequestService;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateExchangeRequestServiceImpl implements CreateExchangeRequestService {

    private final SaveExchangeRequestPort saveExchangeRequestPort;
    private final PublishEventPort publishEventPort;

    @Override
    @Transactional
    public ExchangeRequest createExchangeRequest(ExchangeRequest toBeSaved) {
        var saved = saveExchangeRequestPort.save(toBeSaved);
        saved.markExchangeCurrencyStarted();
        publishEventPort.publish(saved);
        return saved;
    }
}
