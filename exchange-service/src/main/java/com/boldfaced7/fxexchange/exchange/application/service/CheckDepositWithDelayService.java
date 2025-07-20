package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckDepositWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckDepositWithDelayUseCase;
import com.boldfaced7.fxexchange.exchange.application.port.out.cache.LoadExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.domain.exception.ExchangeRequestNotFoundException;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class CheckDepositWithDelayService implements CheckDepositWithDelayUseCase {

    private final LoadExchangeRequestCachePort loadExchangeRequestCachePort;
    private final PublishEventPort publishEventPort;

    @Override
    public void checkDepositWithDelay(CheckDepositWithDelayCommand command) {
        var exchange = getExchange(command.exchangeId());
        publishExchangeEvent(exchange, command.count());
    }

    private ExchangeRequest getExchange(ExchangeId exchangeId) {
        return loadExchangeRequestCachePort.loadByExchangeId(exchangeId)
                .orElseThrow(() -> new ExchangeRequestNotFoundException(exchangeId));
    }

    private void publishExchangeEvent(ExchangeRequest exchange, Count count) {
        exchange.markDepositUnknown(count);
        publishEventPort.publish(exchange);
    }

}
