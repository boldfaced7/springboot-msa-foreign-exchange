package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckWithdrawalWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.CheckWithdrawalWithDelayUseCase;
import com.boldfaced7.fxexchange.exchange.application.port.out.cache.LoadExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.domain.exception.ExchangeRequestNotFoundException;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class CheckWithdrawalWithDelayService implements CheckWithdrawalWithDelayUseCase {

    private final LoadExchangeRequestCachePort loadExchangeRequestCachePort;
    private final PublishEventPort publishEventPort;

    @Override
    public void checkWithdrawalWithDelay(CheckWithdrawalWithDelayCommand command) {
        var exchange = getExchange(command.exchangeId());
        publishExchangeEvent(exchange, command.count());
    }

    private ExchangeRequest getExchange(ExchangeId exchangeId) {
        return loadExchangeRequestCachePort.loadByExchangeId(exchangeId)
                .orElseThrow(() -> new ExchangeRequestNotFoundException(exchangeId));
    }

    private void publishExchangeEvent(ExchangeRequest exchange, Count count) {
        exchange.markWithdrawalUnknown(count);
        publishEventPort.publish(exchange);
    }

}
